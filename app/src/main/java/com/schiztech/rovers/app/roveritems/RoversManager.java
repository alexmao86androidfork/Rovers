package com.schiztech.rovers.app.roveritems;

import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.os.Handler;
import android.os.Looper;
import android.widget.Toast;

import com.crashlytics.android.Crashlytics;
import com.google.gson.Gson;
import com.schiztech.rovers.app.R;
import com.schiztech.rovers.app.activities.roversactions.creators.CreateHomeActionActivity;
import com.schiztech.rovers.app.activities.roversactions.creators.CreateSearchActionActivity;
import com.schiztech.rovers.app.utils.RoversUtils;
import com.schiztech.rovers.app.utils.json.InterfaceAdapter;
import com.schiztech.rovers.app.utils.LogUtils;
import com.schiztech.rovers.app.utils.RoverlyticsUtils;
import com.schiztech.rovers.app.utils.Utils;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.Stack;

/**
 * Created by schiz_000 on 5/12/2014.
 * Singelton calss in charge of managing the folders and items, navigating, saving & announcing changes.
 */
public class RoversManager {
    private static final String TAG = LogUtils.makeLogTag("RoversManager");

    private FolderRover mRootFolder;
    private Stack<FolderRover> mFolderHistoryStack = new Stack<FolderRover>();

    private Context mApplicationContext;

    private boolean mIsLoaded = false;

    //region Shared Prefs Members

    public static final long ROOT_FOLDER_ID = -1;

//    private SharedPreferences mItemsPreferences;
//    private static final String PREFS_FILE_ROVER_ITEMS = "rover_items_prefs";
    public static final String PREF_IS_INITIALIZED = "is_initialized";
    public static final String PREF_ROOT_FOLDER_ROVERS = "root_folder_rovers";
    public static final String PREF_FOLDER_ID_GENERATOR_KEY = "folder_id_generator";
    public static final String PREF_ROVER_ID_GENERATOR_KEY = "rover_id_generator";
    public static final String PREF_BITMAP_ID_GENERATORKEY = "bitmap_id_generator";

    //endregion

    //region Singelton Fields & C'tor

    private static RoversManager sInstance;
    public static RoversManager getInstance(Context applicationContext){
        if(sInstance == null) {
            sInstance = new RoversManager(applicationContext);
        }

        return sInstance;
    }
    private RoversManager(Context applicationContext) {

        this.mApplicationContext = applicationContext;
//        mItemsPreferences = getItemsSharedPreferences(mApplicationContext);
    }


    //endregion

    public void init(){
            loadRovers();
    }

    public int countRoot(){
        return countFolderRoverChildren(mRootFolder);
    }

    public int countFolderRoverChildren(FolderRover folderRover){
        if(folderRover == null) return 0;
        int count = 0;
        for(IRover rover : folderRover.getChildren()){
            count++;
            if(rover instanceof FolderRover){
                count += countFolderRoverChildren((FolderRover)rover);
            }
        }

        return count;
    }

    private static SharedPreferences getItemsSharedPreferences(Context context){
        return context.getSharedPreferences(Utils.getString(context, R.string.prefs_items), Context.MODE_MULTI_PROCESS);

    }

    public static long generateNewRoverID(Context context){
        SharedPreferences prefs = getItemsSharedPreferences(context);
        long prevID = prefs.getLong(PREF_ROVER_ID_GENERATOR_KEY, -1);
        prefs.edit().putLong(PREF_ROVER_ID_GENERATOR_KEY, prevID + 1).commit();

        return prevID + 1;
    }

    public static long generateNewBitmapID(Context context) {
        SharedPreferences prefs = getItemsSharedPreferences(context);
        long prevID = prefs.getLong(RoversManager.PREF_BITMAP_ID_GENERATORKEY, 0);
        prefs.edit().putLong(RoversManager.PREF_BITMAP_ID_GENERATORKEY, prevID + 1).commit();

        return prevID + 1;
    }

    //region Rover Gson Converters

    public static String roverToGson(IRover rover){
        Gson gson = InterfaceAdapter.getBuiltGsonObject();
        String gsonRover = gson.toJson(rover);

        return gsonRover;
    }

    public static <T extends IRover> IRover gsonToRover(String roverGson, @NotNull Class<T> cls){
        //assume that nobody will be stupid enough to send T non IRover...
            Gson gson = InterfaceAdapter.getBuiltGsonObject();
            IRover rover = (IRover)gson.fromJson(roverGson, cls);

            return rover;
    }

    //endregion

    //region Folder Navigation Methods
    public static long generateNewFolderID(Context context){
        SharedPreferences prefs = getItemsSharedPreferences(context);
        long prevID = prefs.getLong(PREF_FOLDER_ID_GENERATOR_KEY, ROOT_FOLDER_ID);
        prefs.edit().putLong(PREF_FOLDER_ID_GENERATOR_KEY, prevID + 1).commit();

        return prevID + 1;
    }

    public void navigateToRoot(){
        LogUtils.LOGI(TAG, "Navigate to Root Folder");
        if(!isCurrentFolderRoot()) {
            mFolderHistoryStack.clear();
            mFolderHistoryStack.push(mRootFolder);
        }
        notifyOnChangeListeners(ACTION_ROVER_FOLDER, mRootFolder, -1);
    }

    public boolean isCurrentFolderRoot(){

        return mFolderHistoryStack.peek() == mRootFolder;
    }

    public boolean isCurrentFolderLastInStack(){
        return mFolderHistoryStack.empty();
    }

    public boolean navigateBack(){
        if(mFolderHistoryStack.empty())
            return false;

        mFolderHistoryStack.pop();

        notifyOnChangeListeners(ACTION_ROVER_FOLDER, mFolderHistoryStack.peek(), -1);

        return true;
    }

    /**
     * Navigates to an inner folder inside the currentFolder which has the same ID
     * as the parameter given
     * @param folderID - the id of the folder to navigate
     */
    public void navigateInner(long folderID){
        if(!mFolderHistoryStack.empty()) {
            for (IRover item : mFolderHistoryStack.peek().getChildren()) {

                //if item is folder, AND with the desired folder ID
                if (item instanceof FolderRover &&
                        ((FolderRover) item).getFolderID() == folderID) {

                    //add desired folder to stack
                    mFolderHistoryStack.push( (FolderRover) item );
                    notifyOnChangeListeners(ACTION_ROVER_FOLDER, item, -1);
                    return;//we're done here
                }
            }
        }

        //if the foreach didn't find the folder with the desired ID, it's not here!
        throw new NoSuchFieldError("There's no folder with ID " + folderID +" in the current folder");
    }

    public void navigateToFolder(FolderRover folder){

        mFolderHistoryStack.push(folder);
        notifyOnChangeListeners(ACTION_ROVER_FOLDER, folder, -1);
    }
    //endregion

    //region Current Folder Methods
    public List<IRover> getCurrentFolderChildren(){
        if(mFolderHistoryStack.empty())
            return null;

        return mFolderHistoryStack.peek().getChildren();
    }

    public boolean addChildToCurrentFolder(IRover newChild){
        return addChildToCurrentFolder(newChild, true);
    }
    public boolean addChildToCurrentFolder(IRover newChild, boolean isAutoSave){
        //can't add new rover to a non-folder item or when there's no current folder
        if(mFolderHistoryStack.empty() || !(mFolderHistoryStack.peek() instanceof FolderRover))
            return false;

        FolderRover currentFolder = mFolderHistoryStack.peek();

        boolean isSucceeded = currentFolder.addChild(newChild);

        if(isAutoSave && isSucceeded)
            saveRovers(ACTION_ROVER_ADD, newChild, currentFolder.getChildren().indexOf(newChild));

        if(isSucceeded) {
            //notify child added
            updateRoversCountChanged();
        }

        return isSucceeded;
    }

    public boolean removeChildFromCurrentFolder(IRover child){
        //can't add new rover to a non-folder item or when there's no current folder
        if(mFolderHistoryStack.empty() || !(mFolderHistoryStack.peek() instanceof FolderRover))
            return false;

        FolderRover currentFolder = mFolderHistoryStack.peek();
        int oldPosition = currentFolder.getChildren().indexOf(child);
        boolean isSucceeded = currentFolder.removeChild(child);

        if(isSucceeded) {
            saveRovers(ACTION_ROVER_REMOVE, child, oldPosition);
            updateRoversCountChanged();
        }

        return isSucceeded;

    }

    public boolean removeChildFromCurrentFolder(int position){
        //can't add new rover to a non-folder item or when there's no current folder
        if(mFolderHistoryStack.empty() || !(mFolderHistoryStack.peek() instanceof FolderRover))
            return false;
        boolean isSucceeded = false;
        IRover deletedRover = null;

        FolderRover currentFolder = mFolderHistoryStack.peek();
        if(currentFolder.getChildren() != null && currentFolder.getChildren().size() > position) {
            deletedRover = currentFolder.getChildren().get(position);
            isSucceeded = currentFolder.removeChildAt(position);
        }

        if(isSucceeded) {
            saveRovers(ACTION_ROVER_REMOVE, deletedRover, position);
            updateRoversCountChanged();
        }
        return isSucceeded;
    }

    private void updateRoversCountChanged(){
        if(mApplicationContext != null) {
            RoverlyticsUtils.setItemsCount(mApplicationContext, countRoot());
        }
    }

    public boolean repositionChildInCurrentFolder(int currentPosition, int newPosition){
        //can't add new rover to a non-folder item or when there's no current folder
        if(mFolderHistoryStack.empty() || !(mFolderHistoryStack.peek() instanceof FolderRover))
            return false;


        FolderRover currentFolder = mFolderHistoryStack.peek();

        //check if current position requested is in bounds
        if(currentFolder.getChildren() != null && currentFolder.getChildren().size() > currentPosition) {
            IRover repositionRover = currentFolder.getChildren().get(currentPosition);

            boolean isSucceeded = currentFolder.removeChildAt(currentPosition);
            if (isSucceeded) {
                isSucceeded = currentFolder.addChild(newPosition, repositionRover);
            }
            if (isSucceeded) {
                saveRovers(ACTION_ROVER_REPOSITION, repositionRover, currentPosition);
            }
            return isSucceeded;
        }
        return false;
    }

    public FolderRover getCurrentFolder(){
        if(mFolderHistoryStack.empty())
            return null;

        return mFolderHistoryStack.peek();
    }

    //endregion

    //region Save & Load Rovers

    private class SaveRoversTask extends AsyncTask<String, Void, Void>{

        int mAction;
        IRover mRover;
        int mPosition;

        public SaveRoversTask(int action, IRover rover, int position){
            mAction = action;
            mRover = rover;
            mPosition = position;

        }
        @Override
        protected Void doInBackground(String... strings) {

            if(mApplicationContext == null) return null;
            try {
                synchronized (mApplicationContext) {
                    Gson gson = InterfaceAdapter.getBuiltGsonObject();
                    String gsonRootFolder = gson.toJson(mRootFolder);
                    SharedPreferences prefs = getItemsSharedPreferences(mApplicationContext);

                    //save items
                    prefs.edit().putString(PREF_ROOT_FOLDER_ROVERS, gsonRootFolder).commit();

                    notifyOnChangeListeners(mAction, mRover, mPosition);

                    mRover = null;//cleanup.
                }
            }
            catch (Exception e){
                    LogUtils.LOGE(TAG, "Error while saving Rovers: " + e.getMessage());
            }
            return null;
        }
    }

    private void saveRovers(int action, IRover rover, int position){
        LogUtils.LOGI(TAG, "Saving Rovers");

        new SaveRoversTask(action, rover, position).execute("");
    }


    private class LoadRoversTask extends  AsyncTask<String,Void,Void>{
        @Override
        protected Void doInBackground(String... strings) {

            //region Sleep 10 - Bug that causes lag on load, for some reason needs a slight delay when showing.
            try {
                Thread.sleep(10);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            //endregion
            if(mIsLoaded){
                navigateToRoot();
            }
            else {
                boolean isFirstTime = handleInitialRovers();

                //if first timer, let the init take care of items. the file still doesn't exists.
                if(!isFirstTime) {
                    loadRoversFromFile();
                }

                mIsLoaded = true;
            }
            return null;
        }

    }

    private void loadRovers() {
        loadRovers(false);
    }

    private void loadRovers(boolean isForced){
        LogUtils.LOGI(TAG, "Loading Rovers " + (isForced? "(Forced)" : "UnForced"));
        if(isForced){
            mIsLoaded = false;
        }
        new LoadRoversTask().execute("");
    }

    private void loadRoversFromFile(){
        LogUtils.LOGI(TAG, "Loading Rovers From File");

        mFolderHistoryStack.clear();
        mRootFolder = null;

        FolderRover loadedRootFolder = null;
        SharedPreferences prefs = getItemsSharedPreferences(mApplicationContext);
        String gsonRootFolder = prefs.getString(PREF_ROOT_FOLDER_ROVERS, null);
        if (gsonRootFolder != null) {
            Gson gson = InterfaceAdapter.getBuiltGsonObject();
            loadedRootFolder = gson.fromJson(gsonRootFolder, FolderRover.class);
        }

        if (loadedRootFolder == null) {
            mRootFolder = new FolderRover(ROOT_FOLDER_ID);
        } else {
            mRootFolder = loadedRootFolder;
        }
        mFolderHistoryStack.push(mRootFolder);
        notifyOnChangeListeners(ACTION_ROVER_FOLDER, mRootFolder, -1);
    }

    private boolean handleInitialRovers(){
        SharedPreferences prefs = getItemsSharedPreferences(mApplicationContext);
        boolean isInitialized = prefs.getBoolean(PREF_IS_INITIALIZED, false);

        if(!isInitialized) {
            //init folder objects
            mRootFolder = new FolderRover(ROOT_FOLDER_ID);
            mFolderHistoryStack.push(mRootFolder);

            //add initial rovers
            addInitialRovers();
            //mark as Initialized.
            prefs.edit().putBoolean(PREF_IS_INITIALIZED, true).apply();
        }

        //if already initialized, nothing was done here.
        return !isInitialized;
    }

    //endregion

    //region Initial Rovers

    private void addInitialRovers(){
        LogUtils.LOGI(TAG, "Adding Initial Rovers");

        //search rover
        Intent searchActionIntent = CreateSearchActionActivity.createSearchActionIntent(mApplicationContext);
        IRover searchRover = new ActionRover(searchActionIntent, mApplicationContext);
        searchRover.setColor(Utils.getColor(mApplicationContext, R.color.md_red_A400));
        addChildToCurrentFolder(searchRover, false);

        //folder rover
        IRover folderFolder = new FolderRover(mApplicationContext);
        addChildToCurrentFolder(folderFolder, false);

        //market rover
        Intent marketIntent = mApplicationContext.getPackageManager().getLaunchIntentForPackage("com.android.vending");
        if(marketIntent != null){
            IRover marketRover = new ApplicationRover(marketIntent, mApplicationContext);
            marketRover.setColor(Utils.getColor(mApplicationContext, R.color.md_green_A700));
            marketRover.setIcon(RoversUtils.RoverIcon.Apps_Google_Play);
            addChildToCurrentFolder(marketRover, false);
        }

        //home rover
        Intent homeActionIntent = CreateHomeActionActivity.createHomeActionIntent(mApplicationContext);
        IRover homeRover = new ActionRover(homeActionIntent, mApplicationContext);
        homeRover.setColor(Utils.getColor(mApplicationContext, R.color.md_blue_A200));
        addChildToCurrentFolder(homeRover, false);


        saveRovers(ACTION_ROVER_FOLDER, mRootFolder, -1);
    }

    //endregion Initial Rovers

    //region OnChange Listener Methods & members

    public static final int ACTION_ROVER_ADD = 1;
    public static final int ACTION_ROVER_REMOVE = 2;
    public static final int ACTION_ROVER_FOLDER = 3;
    public static final int ACTION_ROVER_REPOSITION = 4;

    private List<OnChangeListener> mOnChangeListeners = new ArrayList<OnChangeListener>();
    private Handler mMainThreadHandler = new Handler(Looper.getMainLooper());

    /**
     * Registers a listener to be triggered when either the list of active extensions changes or an
     * extension's data changes.
     */
    public void addOnChangeListener(OnChangeListener onChangeListener) {
        mOnChangeListeners.add(onChangeListener);
    }

    /**
     * Removes a listener previously registered with {@link #addOnChangeListener}.
     */
    public void removeOnChangeListener(OnChangeListener onChangeListener) {
        mOnChangeListeners.remove(onChangeListener);
    }

    private void notifyOnChangeListeners(final int action, final IRover item, final int position) {
        mMainThreadHandler.post(new Runnable() {
            @Override
            public void run() {
                for (OnChangeListener listener : mOnChangeListeners) {
                    switch(action){
                        case ACTION_ROVER_ADD:
                            listener.onRoverAdded(item, position);
                            break;
                        case ACTION_ROVER_REMOVE:
                            listener.onRoverRemoved(item, position);
                            break;
                        case ACTION_ROVER_FOLDER:
                            listener.onRoversFolderChanged(item);
                            break;
                        case ACTION_ROVER_REPOSITION:
                            listener.onRoverRepositioned(item, position, getCurrentFolderChildren().indexOf(item));
                            break;
                    }

                }
            }
        });
    }


    public static interface OnChangeListener {

        /**
         * Called on folder change
         * @param item is the folder that is on front now.
         */
        void onRoversFolderChanged(IRover item);

        /**
         * called when a new rover item is added
         * @param item is the newly added rover
         * @param position is the position of the newly added rover in the current folder
         */
        void onRoverAdded(IRover item, int position);

        /**
         * called when a rover item is removed
         * @param item that is removed from the current folder
         * @param position is the position of the removed rover in the current folder
         */
        void onRoverRemoved(IRover item, int position);

        /**
         *
         * @param item item that is being repositioned
         * @param oldPosition previous location of the item
         * @param newPosition new location of the item
         */
        void onRoverRepositioned(IRover item, int oldPosition, int newPosition);
    }


    //endregion

    public static boolean launchRover(ExecutableRover rover, Context context){
        try {
            Intent executableIntent = rover.getLaunchIntent();
            executableIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            context.startActivity(executableIntent);

            return true;
        }
        catch (ActivityNotFoundException activityException){
            Toast.makeText(context, R.string.rovers_launch_error_lost, Toast.LENGTH_SHORT).show();
        }

        catch (Exception e){
            Toast.makeText(context, R.string.rovers_launch_error_default, Toast.LENGTH_SHORT).show();
        }

        return false;
    }

    public static Drawable getErrorRoverIcon(Context context){
        return context.getResources().getDrawable(R.drawable.ri_error);
    }

}

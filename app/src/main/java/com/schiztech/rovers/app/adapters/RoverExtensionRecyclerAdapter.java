package com.schiztech.rovers.app.adapters;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.schiztech.rovers.app.R;
import com.schiztech.rovers.app.managers.AnalyticsManager;
import com.schiztech.rovers.app.managers.OpenIabManager;
import com.schiztech.rovers.app.utils.ExtensionsUtils;
import com.schiztech.rovers.app.utils.LogUtils;
import com.schiztech.rovers.app.utils.MarketUtils;
import com.schiztech.rovers.app.utils.Utils;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by schiz_000 on 3/13/2015.
 */
public class RoverExtensionRecyclerAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private static final int TYPE_HEADER = 0;
    private static final int TYPE_ITEM = 1;
    private static final int TYPE_COFFEE = 2;

    private boolean mIsShowingCoffee = false;
    private Context mContext;
    List<RoverExtensionHolder> mInfos;

    //region BuyRequestListener

    public interface OnBuyRequestListener {
        void onBuyRequest(ExtensionsUtils.ExtensionType extensionType);
    }

    OnBuyRequestListener mBuyRequestListener;

    public void setBuyRequestListener(OnBuyRequestListener listener){
        mBuyRequestListener = listener;
    }

    private void onBuyRequest(ExtensionsUtils.ExtensionType extensionType){
        if(mBuyRequestListener != null)
            mBuyRequestListener.onBuyRequest(extensionType);
    }

    //endregion BuyRequestListener

    //region ViewHolder

    public static class RoverExtensionHolder {
        ExtensionsUtils.ExtensionType extensionType;
        String hTitle;
        String hDescription;
        String hPrice;
        Drawable hIcon;
        boolean hIsPurchased;

        public static RoverExtensionHolder getInstance() {
            return new RoverExtensionHolder();
        }

        public void updateIsGot(Context context){
            hIsPurchased = ExtensionsUtils.isGotExtension(context, extensionType);
        }
    }

    class RoverExtensionViewHolder extends RecyclerView.ViewHolder {
        protected TextView vTitle;
        protected TextView vDescription;
        protected ImageView vIcon;
        protected TextView vPrice;
        protected View vBuyButton;
        protected View vPurchasedView;

        public RoverExtensionViewHolder(View v) {
            super(v);
            vTitle = (TextView) v.findViewById(R.id.extension_title);
            vDescription = (TextView) v.findViewById(R.id.extension_description);
            vIcon = (ImageView) v.findViewById(R.id.extension_icon);
            vPrice = (TextView) v.findViewById(R.id.extension_price);
            vPurchasedView = v.findViewById(R.id.extension_purchased);
            vBuyButton = v.findViewById(R.id.extension_buyBtn);
            vBuyButton.setOnClickListener(mBuyButtonClick);
        }
    }

    class HeaderExtensionViewHolder extends RecyclerView.ViewHolder {
        public HeaderExtensionViewHolder(View v) {
            super(v);
        }
    }

    class CoffeeExtensionViewHolder extends RecyclerView.ViewHolder {
        protected View vDonateButton;
        protected View vRateButton;

        public CoffeeExtensionViewHolder(View v) {
            super(v);
            vDonateButton = v.findViewById(R.id.coffee_donateButton);
            vDonateButton.setOnClickListener(mBuyButtonClick);

            vRateButton = v.findViewById(R.id.coffee_rateButton);
            vRateButton.setOnClickListener(mRateButtonClick);


        }
    }


    //endregion ViewHolder

    //region RecyclerView Methods

    public RoverExtensionRecyclerAdapter(Context context) {
        mContext = context;
        updateCoffeeVisibility();
        new LoadRoverExtensionsTask().execute();
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder vh, int i) {
        if(vh instanceof HeaderExtensionViewHolder){
            return;
        }
        if(vh instanceof CoffeeExtensionViewHolder){
            CoffeeExtensionViewHolder coffeeHolder = (CoffeeExtensionViewHolder)vh;
            coffeeHolder.vDonateButton.setTag(ExtensionsUtils.ExtensionType.Coffee);

            return;
        }

        if (vh instanceof RoverExtensionViewHolder) {
            RoverExtensionViewHolder viewHolder = (RoverExtensionViewHolder)vh;

            final RoverExtensionHolder holder = getItem(i);

            viewHolder.vTitle.setText(holder.hTitle);
            viewHolder.vTitle.setSelected(true);
            viewHolder.vIcon.setImageDrawable(holder.hIcon);
            viewHolder.vDescription.setText(holder.hDescription);
            viewHolder.vPrice.setText(holder.hPrice);
            viewHolder.vBuyButton.setTag(holder.extensionType);

            if (holder.hIsPurchased) {
                viewHolder.vPurchasedView.setVisibility(View.VISIBLE);
                viewHolder.vPrice.setVisibility(View.GONE);
                viewHolder.vBuyButton.setEnabled(false);
                viewHolder.vBuyButton.setAlpha(0.25f);
                viewHolder.vBuyButton.setOnClickListener(null);
            } else {
                viewHolder.vPurchasedView.setVisibility(View.GONE);
                viewHolder.vPrice.setVisibility(View.VISIBLE);
                viewHolder.vBuyButton.setEnabled(true);
                viewHolder.vBuyButton.setAlpha(1);
            }

        }
    }

    @Override
    public int getItemCount() {
        if (mInfos == null) return 1;
        if(mIsShowingCoffee)
            return mInfos.size() + 2;
        else
            return mInfos.size() + 1;
    }

    @Override
    public int getItemViewType(int position) {
        if (isPositionHeader(position))
            return TYPE_HEADER;

        if(isPositionCoffee(position)) {
            return TYPE_COFFEE;
        }

        return TYPE_ITEM;
    }

    private boolean isPositionHeader(int position) {
        return position == 0;
    }

    private boolean isPositionCoffee(int position){
        if(mIsShowingCoffee) {
            return position == 1;
        }
        return false;
    }

    private RoverExtensionHolder getItem(int position) {
        if(mIsShowingCoffee)
            return mInfos.get(position - 2);

        return mInfos.get(position - 1);
    }

    public void clear() {
        if (mInfos != null)
            mInfos.clear();
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup viewGroup, int viewType) {
        if (viewType == TYPE_ITEM) {
            View itemView = LayoutInflater.
                    from(viewGroup.getContext()).
                    inflate(R.layout.list_item_extension, viewGroup, false);
            return new RoverExtensionViewHolder(itemView);
        } else if (viewType == TYPE_HEADER) {
            View itemView = LayoutInflater.
                    from(viewGroup.getContext()).
                    inflate(R.layout.header_item_extensions, viewGroup, false);
            return new HeaderExtensionViewHolder(itemView);
        }
        else if(viewType == TYPE_COFFEE){
            View itemView = LayoutInflater.
                    from(viewGroup.getContext()).
                    inflate(R.layout.header_item_coffee, viewGroup, false);
            return new CoffeeExtensionViewHolder(itemView);
        }



        throw new RuntimeException("there is no type that matches the type " + viewType + " + make sure your using types correctly");

    }

    //endregion RecyclerView Methods

    private View.OnClickListener mBuyButtonClick = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            if(view.getTag() instanceof ExtensionsUtils.ExtensionType){
                onBuyRequest((ExtensionsUtils.ExtensionType)view.getTag());
            }
        }
    };

    private View.OnClickListener mRateButtonClick = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            if(mContext == null) return;
            AnalyticsManager.getInstance(mContext).reportEvent(AnalyticsManager.Category.UX,
                    AnalyticsManager.Action.Button_Click,
                    "Extensions_Rate");

            Utils.browseLink(mContext,MarketUtils.getPackagePlayStoreLink(mContext.getPackageName(),false));
        }
    };


    //region Coffee

    private void updateCoffeeVisibility(){
        if(ExtensionsUtils.isGotCompletePackage(mContext) || ExtensionsUtils.isGotAllSeparated(mContext))
            mIsShowingCoffee = true;
    }

    //endregion Coffee

    //region Extensions Data

    private class LoadRoverExtensionsTask extends AsyncTask<Void, Void, Void> {

        @Override
        protected void onPreExecute() {
            mInfos = new ArrayList<>();
        }

        @Override
        protected Void doInBackground(Void... voids) {

            RoverExtensionHolder h1 = RoverExtensionHolder.getInstance();
            h1.extensionType = ExtensionsUtils.ExtensionType.MoreSettings;
            h1.hTitle = Utils.getString(mContext, R.string.extensions_more_settings_title);
            h1.hDescription = Utils.getString(mContext, R.string.extensions_more_settings_desc);
            h1.hIcon = Utils.getDrawable(mContext, R.drawable.ic_extensions_moresettings);
            h1.hPrice = OpenIabManager.getInstance(mContext).getExtensionPrice(h1.extensionType);

            RoverExtensionHolder h2 = RoverExtensionHolder.getInstance();
            h2.extensionType = ExtensionsUtils.ExtensionType.MoreColors;
            h2.hTitle = Utils.getString(mContext, R.string.extensions_more_colors_title);
            h2.hDescription = Utils.getString(mContext, R.string.extensions_more_colors_desc);
            h2.hIcon = Utils.getDrawable(mContext, R.drawable.ic_extensions_morecolors);
            h2.hPrice = OpenIabManager.getInstance(mContext).getExtensionPrice(h2.extensionType);

            RoverExtensionHolder h3 = RoverExtensionHolder.getInstance();
            h3.extensionType = ExtensionsUtils.ExtensionType.MoreRovers;
            h3.hTitle = Utils.getString(mContext, R.string.extensions_more_rovers_title);
            h3.hDescription = Utils.getString(mContext, R.string.extensions_more_rovers_desc);
            h3.hIcon = Utils.getDrawable(mContext, R.drawable.ic_extensions_morerovers);
            h3.hPrice = OpenIabManager.getInstance(mContext).getExtensionPrice(h3.extensionType);

            RoverExtensionHolder h4 = RoverExtensionHolder.getInstance();
            h4.extensionType = ExtensionsUtils.ExtensionType.CompletePackage;
            h4.hTitle = Utils.getString(mContext, R.string.extensions_complete_package_title);
            h4.hDescription = Utils.getString(mContext, R.string.extensions_complete_package_desc);
            h4.hIcon = Utils.getDrawable(mContext, R.drawable.ic_extensions_package);
            h4.hPrice = OpenIabManager.getInstance(mContext).getExtensionPrice(h4.extensionType);


            mInfos.add(h4);//complete package goes first.

            mInfos.add(h1);
            mInfos.add(h2);
            mInfos.add(h3);


            return null;
        }

        @Override
        protected void onPostExecute(Void result) {

            updateInfosIsGot();
            //notifyDataSetChanged();// called on updateInfosIsGot()
        }

    }

    public void updateInfosIsGot(){
        for(RoverExtensionHolder holder : mInfos){
            holder.updateIsGot(mContext);
        }

        updateCoffeeVisibility();
        notifyDataSetChanged();
    }


    //endregion Extensions Data


}

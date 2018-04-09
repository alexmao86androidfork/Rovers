package com.schiztech.rovers.app.adapters;

import android.app.AlertDialog;
import android.content.Context;
import android.os.AsyncTask;
import android.support.v7.widget.RecyclerView;
import android.text.SpannableString;
import android.text.method.LinkMovementMethod;
import android.text.util.Linkify;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.schiztech.rovers.app.R;
import com.schiztech.rovers.app.managers.AnalyticsManager;
import com.schiztech.rovers.app.utils.LogUtils;
import com.schiztech.rovers.app.utils.MarketUtils;
import com.schiztech.rovers.app.utils.Utils;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by schiz_000 on 5/10/2015.
 */
public class CreditsRecyclerAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> implements View.OnClickListener{
    private static final String TAG = LogUtils.makeLogTag("CreditsRecyclerAdapter");

    private static final int TYPE_HEADER = 0;
    private static final int TYPE_ITEM = 1;

    List<CreditHolder> mInfos;
    private Context mContext;
    private Context mActivityContext;

    //region Initialize
    public CreditsRecyclerAdapter(Context context, OnCreditsLoadedListener listener) {
        mListener = listener;
        init(context);
    }

    public CreditsRecyclerAdapter(Context context) {
        init(context);
    }

    private void init(Context context) {
        mActivityContext = context;
        mContext = mActivityContext.getApplicationContext();
        new LoadCreditsTask().execute();
    }
    //endregion Initialize

    //region On Credits Loaded
    private OnCreditsLoadedListener mListener;

    public interface OnCreditsLoadedListener {
        void OnCreditsLoaded(int libraries, int icons);
    }

    public void clearOnCreditsLoadedListener() {
        mListener = null;
    }

    private void callOnCreditsLoadedListener(int libraries, int icons) {
        if (mListener != null) {
            mListener.OnCreditsLoaded(libraries, icons);
        }
    }

    //endregion On Credits Loaded

    //region ViewHolder

    public static class CreditHolder {
        String hTitle;
        String hAuthor;
        String hDescription;
        String hLink;
        int hYear;
        License hLicense;

        public static CreditHolder getInstance() {
            return new CreditHolder();
        }
    }

    class HeaderCreditViewHolder extends RecyclerView.ViewHolder {
        View vPlayButton;
        View vWebsiteButton;
        View vFacebookButton;
        View vTwitterButton;

        public HeaderCreditViewHolder(View v) {
            super(v);
            vPlayButton = v.findViewById(R.id.schiztech_playstoreBtn);
            vTwitterButton = v.findViewById(R.id.schiztech_twitterBtn);
            vFacebookButton = v.findViewById(R.id.schiztech_facebookBtn);
            vWebsiteButton = v.findViewById(R.id.schiztech_websiteBtn);
        }
    }

    public static class CreditViewHolder extends RecyclerView.ViewHolder {
        public View vLayout;
        public TextView vTitle;
        public TextView vAuthor;
        public TextView vDescription;
        public TextView vLicense;
        public View vLicenseLayout;


        public CreditViewHolder(View itemView) {
            super(itemView);
            vTitle = (TextView) itemView.findViewById(R.id.credit_title);
            vAuthor = (TextView) itemView.findViewById(R.id.credit_author);
            vDescription = (TextView) itemView.findViewById(R.id.credit_description);
            vLicense = (TextView) itemView.findViewById(R.id.credit_licenseText);
            vLicenseLayout = itemView.findViewById(R.id.credit_licenseLayout);
            vLayout = itemView.findViewById(R.id.credit_layout);
        }
    }

    //endregion ViewHolder

    //region RecyclerView Methods


    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup viewGroup, int viewType) {
        if (viewType == TYPE_ITEM) {
            View itemView = LayoutInflater.
                    from(viewGroup.getContext()).
                    inflate(R.layout.list_item_credit, viewGroup, false);

            return new CreditViewHolder(itemView);
        } else if (viewType == TYPE_HEADER) {
            View itemView = LayoutInflater.
                    from(viewGroup.getContext()).
                    inflate(R.layout.header_item_about, viewGroup, false);

            return new HeaderCreditViewHolder(itemView);
        }


        throw new RuntimeException("there is no type that matches the type " + viewType + " + make sure your using types correctly");
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder vh, int position) {
        if (vh instanceof HeaderCreditViewHolder) {
            HeaderCreditViewHolder headerViewHolder = (HeaderCreditViewHolder) vh;
            headerViewHolder.vPlayButton.setOnClickListener(this);
            headerViewHolder.vTwitterButton.setOnClickListener(this);
            headerViewHolder.vFacebookButton.setOnClickListener(this);
            headerViewHolder.vWebsiteButton.setOnClickListener(this);
        } else if (vh instanceof CreditViewHolder) {
            CreditViewHolder creditViewHolder = (CreditViewHolder) vh;

            final CreditHolder holder = getItem(position);

            creditViewHolder.vTitle.setText(holder.hTitle);
            creditViewHolder.vTitle.setSelected(true);
            creditViewHolder.vAuthor.setText(holder.hAuthor);
            creditViewHolder.vDescription.setText(holder.hDescription);
            creditViewHolder.vLayout.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Utils.browseLink(mContext, holder.hLink);
                }
            });

            if(holder.hLicense != null) {
                creditViewHolder.vLicenseLayout.setVisibility(View.VISIBLE);
                creditViewHolder.vLicense.setText(holder.hLicense.getName());
                creditViewHolder.vLicenseLayout.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {

                        final SpannableString s =
                                new SpannableString(holder.hLicense.getDescription(holder.hYear, holder.hAuthor));
                        Linkify.addLinks(s, Linkify.WEB_URLS);

                        AlertDialog dialog = new AlertDialog.Builder(mActivityContext).setPositiveButton(R.string.dialog_dismiss,null).setMessage(s).create();

                        dialog.show();
                        // Make the textview clickable. Must be called after show()
                        ((TextView)dialog.findViewById(android.R.id.message)).setMovementMethod(LinkMovementMethod.getInstance());


                    }
                });
            }
            else{
                creditViewHolder.vLicenseLayout.setVisibility(View.GONE);
            }

        }
    }

    @Override
    public int getItemCount() {
        if (mInfos == null) return 0;
        return mInfos.size() + 1;
    }

    @Override
    public int getItemViewType(int position) {
        if (isPositionHeader(position))
            return TYPE_HEADER;

        return TYPE_ITEM;
    }

    private boolean isPositionHeader(int position) {
        return position == 0;
    }

    private CreditHolder getItem(int position) {
        return mInfos.get(position - 1);
    }

    public void clear() {
        if (mInfos != null)
            mInfos.clear();
        mContext = null;
        mActivityContext = null;
    }

    //endregion RecyclerView Methods

    //region OnClick

    @Override
    public void onClick(View view) {
        String link;
        String label = "";
        switch (view.getId()){
            case R.id.schiztech_facebookBtn:
                link = Utils.getString(mContext, R.string.link_facebook);
                label = "About_Facebook";
                break;
            case R.id.schiztech_playstoreBtn:
                link = MarketUtils.getPublisherPlayStoreLink("SchizTech", false);
                label = "About_PlayStore";
                break;
            case R.id.schiztech_twitterBtn:
                link = Utils.getString(mContext, R.string.link_twitter);
                label = "About_Twitter";
                break;
            case R.id.schiztech_websiteBtn:
                link = Utils.getString(mContext, R.string.link_website);
                label = "About_Website";
                break;
            default:
                link = null;
        }

        if(link != null){
            Utils.browseLink(mContext, link);
        }

        AnalyticsManager.getInstance(mContext).reportEvent(AnalyticsManager.Category.UX,
                AnalyticsManager.Action.Button_Click,
                label);

    }


    //endregion OnClick

    //region Load Credits
    private class LoadCreditsTask extends AsyncTask<Void, Void, Void> {
        int libraries;

        @Override
        protected void onPreExecute() {
            LogUtils.LOGV(TAG, "loading task preExecute");
            mInfos = new ArrayList<CreditHolder>();
        }

        @Override
        protected Void doInBackground(Void... voids) {

            //Libraries
            mInfos.addAll(getLibraries());
            libraries = mInfos.size();
            mInfos.addAll(getIconPacks());
            return null;
        }

        @Override
        protected void onPostExecute(Void result) {
            LogUtils.LOGV(TAG, "loading task postExecute");

            callOnCreditsLoadedListener(libraries, mInfos.size() - libraries);
            notifyDataSetChanged();
        }


        //region Libraries

        private List<CreditHolder> getLibraries(){
            List<CreditHolder> libs = new ArrayList<>();
            libs.add(getReboundLibraryCredit());
            libs.add(getStandOutCredit());
            libs.add(getOpenIabCredit());
            libs.add(getAcraCredit());
            libs.add(getPreferenceFragmentCredit());
            libs.add(getCircleIndicatorCredit());
            libs.add(getStickyGridHeadersCredit());
            libs.add(getGsonCredit());
            return libs;
        }

        private CreditHolder getReboundLibraryCredit() {
            CreditHolder credit = new CreditHolder();
            credit.hTitle = "Rebound";
            credit.hAuthor = "Facebook, Inc";
            credit.hYear = 2013;
            credit.hDescription = "A Java library that models spring dynamics and adds real world physics to your app.";
            credit.hLink = "http://facebook.github.io/rebound";
            credit.hLicense = new BSDLicense();

            return credit;
        }

        private CreditHolder getPreferenceFragmentCredit() {
            CreditHolder credit = new CreditHolder();
            credit.hTitle = "Support PreferenceFragment";
            credit.hAuthor = "Machinarius";
            credit.hYear = 2013;
            credit.hDescription = "Unofficial PreferenceFragment compatibility layer for Android 1.6 and up.";
            credit.hLink = "https://github.com/Machinarius/PreferenceFragment-Compat";
            credit.hLicense = new Apache2License();

            return credit;
        }

        private CreditHolder getOpenIabCredit() {
            CreditHolder credit = new CreditHolder();
            credit.hTitle = "OpenIAB";
            credit.hAuthor = "onepf";
            credit.hYear = 2012;
            credit.hDescription = "Open In-App Billing for Google Play, SlideMe, Amazon Store, Nokia Store, Samsung Apps, Yandex.Store, Appland, Aptoide, AppMall and Fortumo.";
            credit.hLink = "http://onepf.org/openiab/";
            credit.hLicense = new Apache2License();

            return credit;
        }

        private CreditHolder getCircleIndicatorCredit() {
            CreditHolder credit = new CreditHolder();
            credit.hTitle = "CircleIndicator";
            credit.hAuthor = "ongakuer";
            credit.hYear = 2014;
            credit.hDescription = "A lightweight viewpager indicator like in nexus 5 launcher.";
            credit.hLink = "https://github.com/ongakuer/CircleIndicator";
            credit.hLicense = new Apache2License();

            return credit;
        }

        private CreditHolder getGsonCredit() {
            CreditHolder credit = new CreditHolder();
            credit.hTitle = "Gson";
            credit.hAuthor = "Google Inc";
            credit.hYear = 2008;
            credit.hDescription = "Gson is a Java library that can be used to convert Java Objects into their JSON representation.";
            credit.hLink = "https://github.com/google/gson";
            credit.hLicense = new Apache2License();

            return credit;
        }

        private CreditHolder getAcraCredit() {
            CreditHolder credit = new CreditHolder();
            credit.hTitle = "ACRA";
            credit.hAuthor = "ACRA";
            credit.hYear = 2010;
            credit.hDescription = "Application Crash Reports for Android.";
            credit.hLink = "http://www.acra.ch/";
            credit.hLicense = new Apache2License();

            return credit;
        }

        private CreditHolder getStickyGridHeadersCredit() {
            CreditHolder credit = new CreditHolder();
            credit.hTitle = "StickyGridHeaders";
            credit.hAuthor = "TonicArtos";
            credit.hYear = 2013;
            credit.hDescription = "Android library for Android 2.3+ that provides a GridView that shows items in sections with headers.";
            credit.hLink = "http://www.tonicartos.com/";
            credit.hLicense = new Apache2License();

            return credit;
        }

        private CreditHolder getStandOutCredit() {
            CreditHolder credit = new CreditHolder();
            credit.hTitle = "StandOut";
            credit.hAuthor = "Mark Wei";
            credit.hYear = 2012;
            credit.hDescription = "StandOut lets you easily create floating windows in your Android app.";
            credit.hLink = "http://pingpongboss.github.io/StandOut/";
            credit.hLicense = new MITLicense();

            return credit;
        }


        //endregion Libraries

        //region IconPacks

        private List<CreditHolder> getIconPacks(){
            List<CreditHolder> icons = new ArrayList<>();
            icons.add(getIcons8Credit());
            icons.add(getFontAwesomeCredit());
            icons.add(getFontAwesomeCredit());
            icons.add(getIcons4AndroidCredit());
            icons.add(getTypiconsCredit());
            icons.add(getWireconsCredit());
            return icons;
        }

        private CreditHolder getIcons8Credit() {
            CreditHolder credit = new CreditHolder();
            credit.hTitle = "Icons8";
            credit.hAuthor= "Icons8";
            credit.hDescription = "www.icons8.com";
            credit.hLink = "http://www.icons8.com";
            credit.hLicense = new CC_Unported_NoDerivs();

            return credit;
        }

        private CreditHolder getIcons4AndroidCredit() {
            CreditHolder credit = new CreditHolder();
            credit.hTitle = "Icons4Android";
            credit.hAuthor = "Icons4Android";
            credit.hDescription = "www.icons4android.com";
            credit.hLink = "http://www.icons4android.com/";
            credit.hLicense = new CC_Unported();

            return credit;
        }

        private CreditHolder getTypiconsCredit() {
            CreditHolder credit = new CreditHolder();
            credit.hTitle = "Typicons";
            credit.hAuthor = "Stephen Hutchings";
            credit.hDescription = "www.typicons.com";
            credit.hLink = "http://typicons.com/";
            credit.hLicense = new CC_Unported_ShareAlike();

            return credit;
        }

        private CreditHolder getWireconsCredit() {
            CreditHolder credit = new CreditHolder();
            credit.hTitle = "Pictype";
            credit.hAuthor = "Timothy Miller";
            credit.hDescription = "Pictype Free Vector Icons";
            credit.hLink = "http://graphicriver.net/item/pictype-vector-icons/3917143?ref=tmthymllr";
            credit.hLicense = new CC_Unported_ShareAlike();

            return credit;
        }

        private CreditHolder getFontAwesomeCredit() {
            CreditHolder credit = new CreditHolder();
            credit.hTitle = "FontAwesome";
            credit.hAuthor = "Dave Gandy";
            credit.hDescription = "The iconic font and CSS toolkit";
            credit.hLink = "http://fontawesome.io/";
            credit.hLicense = new CC_Unported();

            return credit;
        }


        //endregion
    }

    //endregion Load Credits

    //region Licenses

    public interface License {
        String getName();

        String getDescription(int year, String owner);
    }

    public class Apache2License implements License {
        @Override
        public String getName() {
            return "Apache License, Version 2.0";
        }

        @Override
        public String getDescription(int year, String owner) {
            return String.format("Copyright %d %s\n" +
                    "\n" +
                    "Licensed under the Apache License, Version 2.0 (the \"License\");\n" +
                    "you may not use this file except in compliance with the License.\n" +
                    "You may obtain a copy of the License at\n" +
                    "\n" +
                    "    http://www.apache.org/licenses/LICENSE-2.0\n" +
                    "\n" +
                    "Unless required by applicable law or agreed to in writing, software\n" +
                    "distributed under the License is distributed on an \"AS IS\" BASIS,\n" +
                    "WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.\n" +
                    "See the License for the specific language governing permissions and\n" +
                    "limitations under the License.\n",year, owner);
        }
    }

    public class BSDLicense implements License{

        @Override
        public String getName() {
            return "BSD License";
        }

        @Override
        public String getDescription(int year, String owner) {
            return String.format("Copyright (c) %d, %s. All rights reserved.\n" +
                    "\n" +
                    "Redistribution and use in source and binary forms, with or without modification, are permitted provided that the following conditions are met:\n" +
                    "\n" +
                    "Redistributions of source code must retain the above copyright notice, this list of conditions and the following disclaimer.\n" +
                    "\n" +
                    "Redistributions in binary form must reproduce the above copyright notice, this list of conditions and the following disclaimer in the documentation and/or other materials provided with the distribution.\n" +
                    "\n" +
                    "THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS \"AS IS\" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.",year, owner);
        }


    }

    public class MITLicense implements License{

        @Override
        public String getName() {
            return "MIT License";
        }

        @Override
        public String getDescription(int year, String owner) {
            return String.format("Copyright (C) %s %s\n" +
                    "\n" +
                    "Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated documentation files (the \"Software\"), to deal in the Software without restriction, including without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is furnished to do so, subject to the following conditions:\n" +
                    "\n" +
                    "The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.\n" +
                    "\n" +
                    "THE SOFTWARE IS PROVIDED \"AS IS\", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.",year, owner);
        }

    }

    public class CC_Unported_NoDerivs implements License{

        @Override
        public String getName() {
            return "Creative Commons (Attribution-NoDerivs 3.0 Unported)";
        }

        @Override
        public String getDescription(int year, String owner) {
            return "Full license details can be viewed here: http://creativecommons.org/licenses/by-nd/3.0/legalcode";
        }
    }

    public class CC_Unported implements License{

        @Override
        public String getName() {
            return "Creative Commons (Attribution 3.0 Unported)";
        }

        @Override
        public String getDescription(int year, String owner) {
            return "Full license details can be viewed here: http://creativecommons.org/licenses/by/3.0/legalcode";
        }
    }

    public class CC_Unported_ShareAlike implements License{

        @Override
        public String getName() {
            return "Creative Commons (Attribution-ShareAlike 3.0 Unported)";
        }

        @Override
        public String getDescription(int year, String owner) {
            return "Full license details can be viewed here: http://creativecommons.org/licenses/by-sa/3.0/legalcode";
        }
    }


    //endregion Licenses

}
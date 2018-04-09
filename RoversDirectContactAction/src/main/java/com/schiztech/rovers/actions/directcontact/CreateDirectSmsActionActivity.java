package com.schiztech.rovers.actions.directcontact;


import com.schiztech.roverdirectcontactaction.R;

public class CreateDirectSmsActionActivity extends DirectTypeActionActivityBase {
    public static final Utils.DirectType DIRECT_TYPE = Utils.DirectType.Sms;

    @Override
    protected Utils.DirectType getDirectType() {
        return DIRECT_TYPE;
    }


    @Override
    protected int getIconResource() {
        return R.drawable.ic_sms_green;
    }
}

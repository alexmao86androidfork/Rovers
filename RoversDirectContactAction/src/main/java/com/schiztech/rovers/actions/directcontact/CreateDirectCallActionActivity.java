package com.schiztech.rovers.actions.directcontact;


import com.schiztech.roverdirectcontactaction.R;

public class CreateDirectCallActionActivity extends DirectTypeActionActivityBase {
    public static final Utils.DirectType DIRECT_TYPE = Utils.DirectType.Call;


    @Override
    protected Utils.DirectType getDirectType() {
        return DIRECT_TYPE;
    }

    @Override
    protected int getIconResource() {
        return R.drawable.ic_phone_blue;
    }
}

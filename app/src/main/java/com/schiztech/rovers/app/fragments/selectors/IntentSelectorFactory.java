package com.schiztech.rovers.app.fragments.selectors;

import com.schiztech.rovers.app.utils.Utils;

/**
 * Created by schiz_000 on 9/7/2014.
 */
public class IntentSelectorFactory {
    public static SelectIntentFragment getIntentSelector(Utils.RoverType type){
        switch (type) {
         case App:
            return SelectAppFragment.newInstance();
         case Shortcut:
             return SelectShortcutFragment.newInstance();
        case Action:
            return SelectActionFragment.newInstance();
         default:
             return null;
        }
    }
}

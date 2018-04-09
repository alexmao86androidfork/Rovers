package com.schiztech.rovers.app.utils;

import android.content.Context;

import com.schiztech.rovers.app.R;

/**
 * Created by schiz_000 on 5/17/2015.
 */
public class RoversUtils {
    public enum RoverIcon {

        //region Keys
        NONE,

        //region Apps
        Apps_Android,
        Apps_Calculator,
        Apps_Calendar,
        Apps_Chrome,
        Apps_Dropbox,
        Apps_Drupal,
        Apps_Facebook,
        Apps_Facebook_Square,
        Apps_Flickr,
        Apps_Flickr_Square,
        Apps_Foursquare,
        Apps_Google,
        Apps_Google_Play,
        Apps_Google_Plus,
        Apps_Google_Plus_Square,
        Apps_Hangouts,
        Apps_Instagram,
        Apps_Linkedin,
        Apps_Linkedin_Square,
        Apps_Paypal,
        Apps_Picasa,
        Apps_Pinterest,
        Apps_Pinterest_Square,
        Apps_QQ,
        Apps_QR,
        Apps_QR_Barcode,
        Apps_Reddit,
        Apps_Reddit_Square,
        Apps_Skype,
        Apps_Skype_Circle,
        Apps_SoundCloud,
        Apps_Soundhound,
        Apps_Spotify,
        Apps_Stumbleupon,
        Apps_Stumbleupon_Circle,
        Apps_Tumblr,
        Apps_Tumblr_Square,
        Apps_Twitter,
        Apps_Twitter_Sing,
        Apps_Twitter_Square,
        Apps_Vimeo,
        Apps_Vimeo_Square,
        Apps_Vine,
        Apps_VK,
        Apps_Whatsapp,
        Apps_Winamp,
        Apps_Youtube,
        Apps_Youtube_Play,
        Apps_Youtube_Square,
        Apps_Youtube_TV,

        //endregion Apps

        //region Finance

        Finance_ATM,
        Finance_Chart_Graph,
        Finance_Chart_Pie,
        Finance_Credit_Card,
        Finance_Money_Bag,
        Finance_Money_Bills,
        Finance_Pig_Bank,
        Finance_Shopping_Cart,
        Finance_Star_Percent,
        Finance_Star_Sale,

        //endregion Finance

        //region Folders

        Folders_File,
        Folders_File_Images,
        Folders_File_Music,
        Folders_File_Video,
        Folders_Opened,
        Folders_Round,
        Folders_Round_Camera,
        Folders_Round_Contacts,
        Folders_Round_Downloads,
        Folders_Round_Files,
        Folders_Round_Globe,
        Folders_Round_Heart,
        Folders_Round_Music,
        Folders_Round_Puzzle,
        Folders_Round_Video,

        //endregion Folders

        //region Hardware

        Hardware_Computer_Desktop,
        Hardware_Computer_Laptop,
        Hardware_Game_Controller,
        Hardware_Gameboy,
        Hardware_Phones,
        Hardware_Sim,
        Hardware_Sd,

        //endregion Hardware

        //region Internet

        Internet_Bookmark,
        Internet_Browser_WWW,
        Internet_Cloud_Drive,
        Internet_Download,
        Internet_Globe,

        //endregion Internet

        //region Lifestyle

        Lifestyle_Beer_Glass,
        Lifestyle_Cocktail,
        Lifestyle_Coffee_Mag,
        Lifestyle_Coffee_TA,
        Lifestyle_Cutlery,
        Lifestyle_News,

        //endregion Lifestyle

        //region Location

        Location_Car,
        Location_Gas,
        Location_Globe,
        Location_Map,
        Location_Map_GPS,
        Location_Pin,
        Location_Regular,
        Location_Sign,

        //endregion Location

        //region Mail

        Mail_At,
        Mail_Envelope,

        //endregion Mail

        //region Media

        Media_Camera,
        Media_Camera_Front,
        Media_Camera_Round,
        Media_Camera_Shutter,
        Media_Camera_Video,
        Media_Frame_Landscape,
        Media_Frame_Portraits,
        Media_Frame_Video,
        Media_Movie,
        Media_Panorama,
        Media_Video_Film,
        Media_Video_Play,

        //endregion Media

        //region Messaging

        Messaging_Bubble_MMS,
        Messaging_Bubble_SMS,
        Messaging_SMS,
        Messaging_Dialog,
        Messaging_Monologue,

        //endregion Messaging

        //region Misc
        Misc_Back,
        Misc_Bomb,
        Misc_Broom,
        Misc_Checklist,
        Misc_Crown,
        Misc_Drop,
        Misc_Fire,
        Misc_Flask,
        Misc_Graduation,
        Misc_Home,
        Misc_Flashlight_Off,
        Misc_Flashlight_On,
        Misc_Paw,
        Misc_Pencil,
        Misc_Plant_Leaf,
        Misc_Plant_Stalk,
        Misc_Plant_Tree,
        Misc_Puzzle,
        Misc_Thermometer,
        Misc_Weather_Rain,
        Misc_Weather_Snow,
        Misc_Weather_Sun,


        //endregion Misc

        //region Music

        Music_Equalizer,
        Music_Headphones,
        Music_Headphones_Slim,
        Music_Symbol,
        Music_Symbol_2,

        //endregion Music

        //region Phone

        Phone_Contacts_Book,
        Phone_Dial_Pad,
        Phone_Nexus,
        Phone_Retro,
        Phone_Tube,

        //endregion Phone

        //region Settings

        Settings_Gear,
        Settings_Gear_Ring,
        Settings_Tools,
        Settings_Sliders,

        //endregion Settings

        //region Social

        Social_Contact,
        Social_Contact_Group,
        Social_Heart,
        Social_Like,
        Social_Share,
        Social_Star,
        Social_Star_Dark,
        Social_Star_Half,

        //endregion Social

        //region Sports

        Sports_Baseball,
        Sports_Basketball,
        Sports_Bowling,
        Sports_Cup,
        Sports_Football,
        Sports_Person_Basketball,
        Sports_Person_Bicycle,
        Sports_Person_Hike,
        Sports_Person_Run,
        Sports_Person_Ski,
        Sports_Person_Soccer,
        Sports_Person_Swim,
        Sports_Person_Weight,
        Sports_Person_Yoga,
        Sports_Soccer,
        Sports_Tennis,
        Sports_Volleyball,
        Sports_Weight,

        //endregion Sports

        //region System

        System_Antenna,
        System_Battery_Most,
        System_Bell,
        System_Bluetooth,
        System_Brightness_Half,
        System_Brightness_Full,
        System_Key,
        System_Lock_Close,
        System_Lock_Open,
        System_Microphone,
        System_Plane,
        System_Printer,
        System_Rotation,
        System_Search,
        System_Shutdown,
        System_Sound_On,
        System_Trash,
        System_Vibrate,
        System_Wifi,
        System_Wifi_Lollipop,

        //endregion System

        //region Time

        Time_Alarm,
        Time_Clock,
        Time_Hourglass,
        Time_Stopwatch;

        //endregion Time

        //endregion Keys

        public int getResourceID(){
            switch(this){

                //region Apps
                case Apps_Android:
                    return R.drawable.ri_apps_android;
                case Apps_Calculator:
                    return R.drawable.ri_apps_calculator;
                case Apps_Calendar:
                    return R.drawable.ri_apps_calendar;
                case Apps_Chrome:
                    return R.drawable.ri_apps_chrome;
                case Apps_Dropbox:
                    return R.drawable.ri_apps_dropbox;
                case Apps_Drupal:
                    return R.drawable.ri_apps_drupal;
                case Apps_Facebook:
                    return R.drawable.ri_apps_facebook;
                case Apps_Facebook_Square:
                    return R.drawable.ri_apps_facebook_square;
                case Apps_Flickr:
                    return R.drawable.ri_apps_flickr;
                case Apps_Flickr_Square:
                    return R.drawable.ri_apps_flickr_square;
                case Apps_Foursquare:
                    return R.drawable.ri_apps_foursquare;
                case Apps_Google:
                    return R.drawable.ri_apps_google;
                case Apps_Google_Play:
                    return R.drawable.ri_apps_google_play;
                case Apps_Google_Plus:
                    return R.drawable.ri_apps_google_plus;
                case Apps_Google_Plus_Square:
                    return R.drawable.ri_apps_google_plus_square;
                case Apps_Hangouts:
                    return R.drawable.ri_apps_hangouts;
                case Apps_Instagram:
                    return R.drawable.ri_apps_instagram;
                case Apps_Linkedin:
                    return R.drawable.ri_apps_linkedin;
                case Apps_Linkedin_Square:
                    return R.drawable.ri_apps_linkedin_square;
                case Apps_Paypal:
                    return R.drawable.ri_apps_paypal;
                case Apps_Picasa:
                    return R.drawable.ri_apps_picasa;
                case Apps_Pinterest:
                    return R.drawable.ri_apps_pinterest;
                case Apps_Pinterest_Square:
                    return R.drawable.ri_apps_pinterest_square;
                case Apps_QQ:
                    return R.drawable.ri_apps_qq;
                case Apps_QR:
                    return R.drawable.ri_apps_qr;
                case Apps_QR_Barcode:
                    return R.drawable.ri_apps_qr_barcode;
                case Apps_Reddit:
                    return R.drawable.ri_apps_reddit;
                case Apps_Reddit_Square:
                    return R.drawable.ri_apps_reddit_square;
                case Apps_Skype:
                    return R.drawable.ri_apps_skype;
                case Apps_Skype_Circle:
                    return R.drawable.ri_apps_skype_circle;
                case Apps_SoundCloud:
                    return R.drawable.ri_apps_soundcloud;
                case Apps_Soundhound:
                    return R.drawable.ri_apps_soundhound;
                case Apps_Spotify:
                    return R.drawable.ri_apps_spotify;
                case Apps_Stumbleupon:
                    return R.drawable.ri_apps_stumbleupon;
                case Apps_Stumbleupon_Circle:
                    return R.drawable.ri_apps_stumbleupon_circle;
                case Apps_Tumblr:
                    return R.drawable.ri_apps_tumblr;
                case Apps_Tumblr_Square:
                    return R.drawable.ri_apps_tumblr_square;
                case Apps_Twitter:
                    return R.drawable.ri_apps_twitter;
                case Apps_Twitter_Sing:
                    return R.drawable.ri_apps_twitter_sing;
                case Apps_Twitter_Square:
                    return R.drawable.ri_apps_twitter_square;
                case Apps_Vimeo:
                    return R.drawable.ri_apps_vimeo;
                case Apps_Vimeo_Square:
                    return R.drawable.ri_apps_vimeo_square;
                case Apps_Vine:
                    return R.drawable.ri_apps_vine;
                case Apps_VK:
                    return R.drawable.ri_apps_vk;
                case Apps_Whatsapp:
                    return R.drawable.ri_apps_whatsapp;
                case Apps_Winamp:
                    return R.drawable.ri_apps_winamp;
                case Apps_Youtube:
                    return R.drawable.ri_apps_youtube;
                case Apps_Youtube_Play:
                    return R.drawable.ri_apps_youtube_play;
                case Apps_Youtube_Square:
                    return R.drawable.ri_apps_youtube_square;
                case Apps_Youtube_TV:
                    return R.drawable.ri_apps_youtube_tv;

                //endregion Apps

                //region Finance

                case Finance_ATM:
                    return R.drawable.ri_finance_atm;
                case Finance_Chart_Graph:
                    return R.drawable.ri_finance_chart_graph;
                case Finance_Chart_Pie:
                    return R.drawable.ri_finance_chart_pie;
                case Finance_Credit_Card:
                    return R.drawable.ri_finance_credit_card;
                case Finance_Money_Bag:
                    return R.drawable.ri_finance_money_bag;
                case Finance_Money_Bills:
                    return R.drawable.ri_finance_money_bills;
                case Finance_Pig_Bank:
                    return R.drawable.ri_finance_pig_bank;
                case Finance_Shopping_Cart:
                    return R.drawable.ri_finance_shopping_cart;
                case Finance_Star_Percent:
                    return R.drawable.ri_finance_star_percent;
                case Finance_Star_Sale:
                    return R.drawable.ri_finance_star_sale;

                //endregion Finance

                //region Folders

                case Folders_File:
                    return R.drawable.ri_folders_file;
                case Folders_File_Images:
                    return R.drawable.ri_folders_file_images;
                case Folders_File_Music:
                    return R.drawable.ri_folders_file_music;
                case Folders_File_Video:
                    return R.drawable.ri_folders_file_video;
                case Folders_Opened:
                    return R.drawable.ri_folders_opened;
                case Folders_Round:
                    return R.drawable.ri_folders_round;
                case Folders_Round_Camera:
                    return R.drawable.ri_folders_round_camera;
                case Folders_Round_Contacts:
                    return R.drawable.ri_folders_round_contact;
                case Folders_Round_Downloads:
                    return R.drawable.ri_folders_round_downloads;
                case Folders_Round_Files:
                    return R.drawable.ri_folders_round_files;
                case Folders_Round_Globe:
                    return R.drawable.ri_folders_round_globe;
                case Folders_Round_Heart:
                    return R.drawable.ri_folders_round_heart;
                case Folders_Round_Music:
                    return R.drawable.ri_folders_round_music;
                case Folders_Round_Puzzle:
                    return R.drawable.ri_folders_round_puzzle;
                case Folders_Round_Video:
                    return R.drawable.ri_folders_round_video;

                //endregion Folders

                //region Hardware

                case Hardware_Computer_Desktop:
                    return R.drawable.ri_hardware_computer_desktop;
                case Hardware_Computer_Laptop:
                    return R.drawable.ri_hardware_computer_laptop;
                case Hardware_Game_Controller:
                    return R.drawable.ri_hardware_game_controller;
                case Hardware_Gameboy:
                    return R.drawable.ri_hardware_gameboy;
                case Hardware_Phones:
                    return R.drawable.ri_hardware_phones;
                case Hardware_Sim:
                    return R.drawable.ri_hardware_sim;
                case Hardware_Sd:
                    return R.drawable.ri_hardware_sd;

                //endregion Hardware

                //region Internet

                case Internet_Bookmark:
                    return R.drawable.ri_internet_bookmark;
                case Internet_Browser_WWW:
                    return R.drawable.ri_internet_browser_www;
                case Internet_Cloud_Drive:
                    return R.drawable.ri_internet_cloud_drive;
                case Internet_Download:
                    return R.drawable.ri_internet_download;
                case Internet_Globe:
                    return R.drawable.ri_internet_globe;

                //endregion Internet

                //region Lifestyle

                case Lifestyle_Beer_Glass:
                    return R.drawable.ri_lifestyle_beer_glass;
                case Lifestyle_Cocktail:
                    return R.drawable.ri_lifestyle_cocktail;
                case Lifestyle_Coffee_Mag:
                    return R.drawable.ri_lifestyle_coffee_mag;
                case Lifestyle_Coffee_TA:
                    return R.drawable.ri_lifestyle_coffee_ta;
                case Lifestyle_Cutlery:
                    return R.drawable.ri_lifestyle_cutlery;
                case Lifestyle_News:
                    return R.drawable.ri_lifestyle_news;

                //endregion Lifestyle

                //region Location

                case Location_Car:
                    return R.drawable.ri_location_car;
                case Location_Gas:
                    return R.drawable.ri_location_gas;
                case Location_Globe:
                    return R.drawable.ri_location_globe;
                case Location_Map:
                    return R.drawable.ri_location_map;
                case Location_Map_GPS:
                    return R.drawable.ri_location_map_gps;
                case Location_Pin:
                    return R.drawable.ri_location_pin;
                case Location_Regular:
                    return R.drawable.ri_location_gps_regular;
                case Location_Sign:
                    return R.drawable.ri_location_sign;

                //endregion Location

                //region Mail

                case Mail_At:
                    return R.drawable.ri_mail_at;
                case Mail_Envelope:
                    return R.drawable.ri_mail_envelope;

                //endregion Mail

                //region Media

                case Media_Camera:
                    return R.drawable.ri_media_camera;
                case Media_Camera_Front:
                    return R.drawable.ri_media_camera_front;
                case Media_Camera_Round:
                    return R.drawable.ri_media_camera_round;
                case Media_Camera_Shutter:
                    return R.drawable.ri_media_camera_shutter;
                case Media_Camera_Video:
                    return R.drawable.ri_media_camera_video;
                case Media_Frame_Landscape:
                    return R.drawable.ri_media_frame_landscape;
                case Media_Frame_Portraits:
                    return R.drawable.ri_media_frame_portraits;
                case Media_Frame_Video:
                    return R.drawable.ri_media_frame_video;
                case Media_Movie:
                    return R.drawable.ri_media_movie;
                case Media_Panorama:
                    return R.drawable.ri_media_panorama;
                case Media_Video_Film:
                    return R.drawable.ri_media_video_film;
                case Media_Video_Play:
                    return R.drawable.ri_media_video_play;

                //endregion Media

                //region Messaging

                case Messaging_Bubble_MMS:
                    return R.drawable.ri_messaging_bubble_mms;
                case Messaging_Bubble_SMS:
                    return R.drawable.ri_messaging_bubble_sms;
                case Messaging_SMS:
                    return R.drawable.ri_messaging_sms;
                case Messaging_Dialog:
                    return R.drawable.ri_messaging_dialog;
                case Messaging_Monologue:
                    return R.drawable.ri_messaging_monolog;

                //endregion Messaging

                //region Misc
                case Misc_Back:
                    return R.drawable.ri_misc_back;
                case Misc_Bomb:
                    return R.drawable.ri_misc_bomb;
                case Misc_Broom:
                    return R.drawable.ri_misc_broom;
                case Misc_Checklist:
                    return R.drawable.ri_misc_checklist;
                case Misc_Crown:
                    return R.drawable.ri_misc_book;
                case Misc_Drop:
                    return R.drawable.ri_misc_crown;
                case Misc_Fire:
                    return R.drawable.ri_misc_fire;
                case Misc_Flask:
                    return R.drawable.ri_misc_flask;
                case Misc_Graduation:
                    return R.drawable.ri_misc_graduation;
                case Misc_Home:
                    return R.drawable.ri_misc_home;
                case Misc_Flashlight_Off:
                    return R.drawable.ri_misc_flashlight_off;
                case Misc_Flashlight_On:
                    return R.drawable.ri_misc_flashlight_on;
                case Misc_Paw:
                    return R.drawable.ri_misc_paw;
                case Misc_Pencil:
                    return R.drawable.ri_misc_pencil;
                case Misc_Plant_Leaf:
                    return R.drawable.ri_misc_plant_leaf;
                case Misc_Plant_Stalk:
                    return R.drawable.ri_misc_plant_stalk;
                case Misc_Plant_Tree:
                    return R.drawable.ri_misc_plant_tree;
                case Misc_Puzzle:
                    return R.drawable.ri_misc_puzzle;
                case Misc_Thermometer:
                    return R.drawable.ri_misc_thermometer;
                case Misc_Weather_Rain:
                    return R.drawable.ri_misc_weather_rain;
                case Misc_Weather_Snow:
                    return R.drawable.ri_misc_weather_snow;
                case Misc_Weather_Sun:
                    return R.drawable.ri_misc_weather_sun;

                //endregion Misc

                //region Music

                case Music_Equalizer:
                    return R.drawable.ri_music_equalizer;
                case Music_Headphones:
                    return R.drawable.ri_music_headphones;
                case Music_Headphones_Slim:
                    return R.drawable.ri_music_headphones_slim;
                case Music_Symbol:
                    return R.drawable.ri_music_symbol;
                case Music_Symbol_2:
                    return R.drawable.ri_music_symbol_2;

                //endregion Music

                //region Phone

                case Phone_Contacts_Book:
                    return R.drawable.ri_phone_contacts_book;
                case Phone_Dial_Pad:
                    return R.drawable.ri_phone_dial_pad;
                case Phone_Nexus:
                    return R.drawable.ri_phone_nexus;
                case Phone_Retro:
                    return R.drawable.ri_phone_retro;
                case Phone_Tube:
                    return R.drawable.ri_phone_tube;

                //endregion Phone

                //region Settings

                case Settings_Gear:
                    return R.drawable.ri_settings_gear;
                case Settings_Gear_Ring:
                    return R.drawable.ri_settings_gear_ring;
                case Settings_Tools:
                    return R.drawable.ri_settings_tools;
                case Settings_Sliders:
                    return R.drawable.ri_settings_sliders;

                //endregion Settings

                //region Social

                case Social_Contact:
                    return R.drawable.ri_social_contact;
                case Social_Contact_Group:
                    return R.drawable.ri_social_contact_group;
                case Social_Heart:
                    return R.drawable.ri_social_heart;
                case Social_Like:
                    return R.drawable.ri_social_like;
                case Social_Share:
                    return R.drawable.ri_social_share;
                case Social_Star:
                    return R.drawable.ri_social_star;
                case Social_Star_Dark:
                    return R.drawable.ri_social_star_dark;
                case Social_Star_Half:
                    return R.drawable.ri_social_star_half;

                //endregion Social

                //region Sports

                case Sports_Baseball:
                    return R.drawable.ri_sports_baseball;
                case Sports_Basketball:
                    return R.drawable.ri_sports_basketball;
                case Sports_Bowling:
                    return R.drawable.ri_sports_bowling;
                case Sports_Cup:
                    return R.drawable.ri_sports_cup;
                case Sports_Football:
                    return R.drawable.ri_sports_football;
                case Sports_Person_Basketball:
                    return R.drawable.ri_sports_person_basketball;
                case Sports_Person_Bicycle:
                    return R.drawable.ri_sports_person_bicycle;
                case Sports_Person_Hike:
                    return R.drawable.ri_sports_person_hike;
                case Sports_Person_Run:
                    return R.drawable.ri_sports_person_run;
                case Sports_Person_Ski:
                    return R.drawable.ri_sports_person_ski;
                case Sports_Person_Soccer:
                    return R.drawable.ri_sports_person_soccer;
                case Sports_Person_Swim:
                    return R.drawable.ri_sports_person_swim;
                case Sports_Person_Weight:
                    return R.drawable.ri_sports_person_weight;
                case Sports_Person_Yoga:
                    return R.drawable.ri_sports_person_yoga;
                case Sports_Soccer:
                    return R.drawable.ri_sports_soccer;
                case Sports_Tennis:
                    return R.drawable.ri_sports_tennis;
                case Sports_Volleyball:
                    return R.drawable.ri_sports_volleyball;
                case Sports_Weight:
                    return R.drawable.ri_sports_weight;

                //endregion Sports

                //region System

                case System_Antenna:
                    return R.drawable.ri_system_antenna;
                case System_Battery_Most:
                    return R.drawable.ri_system_battery_most;
                case System_Bell:
                    return R.drawable.ri_system_bell;
                case System_Bluetooth:
                    return R.drawable.ri_system_bluetooth;
                case System_Brightness_Half:
                    return R.drawable.ri_system_brightness_half;
                case System_Brightness_Full:
                    return R.drawable.ri_system_brightness_full;
                case System_Key:
                    return R.drawable.ri_system_key;
                case System_Lock_Close:
                    return R.drawable.ri_system_lock_close;
                case System_Lock_Open:
                    return R.drawable.ri_system_lock_open;
                case System_Microphone:
                    return R.drawable.ri_system_microphone;
                case System_Plane:
                    return R.drawable.ri_system_plane;
                case System_Printer:
                    return R.drawable.ri_system_printer;
                case System_Rotation:
                    return R.drawable.ri_system_rotation;
                case System_Search:
                    return R.drawable.ri_system_search;
                case System_Shutdown:
                    return R.drawable.ri_system_shutdown;
                case System_Sound_On:
                    return R.drawable.ri_system_sound_on;
                case System_Trash:
                    return R.drawable.ri_system_trash;
                case System_Vibrate:
                    return R.drawable.ri_system_vibrate;
                case System_Wifi:
                    return R.drawable.ri_system_wifi;
                case System_Wifi_Lollipop:
                    return R.drawable.ri_system_wifi_lollipop;

                //endregion System

                //region Time

                case Time_Alarm:
                    return R.drawable.ri_time_alarm;
                case Time_Clock:
                    return R.drawable.ri_time_clock;
                case Time_Hourglass:
                    return R.drawable.ri_time_hourglass;
                case Time_Stopwatch:
                    return R.drawable.ri_time_stopwatch;

                //endregion Time

                default:
                    return R.drawable.ri_error;
            }
        }

    }

    public static int getRoverDefaultSize(Context context){
        return Utils.getDimensionPixelSize(context, R.dimen.rovers_icon_size);
    }

    public static int getRoverSize(Context context){
        int defaultSize = getRoverDefaultSize(context);
        int scaleRatio = PrefUtils.getItemsItemSizeValue(context);

        return (int)(defaultSize * (float)(scaleRatio/100f));
    }

    public static int getTriggerSize(Context context){
        boolean isIndependent = PrefUtils.getTriggerIndependentSizeValue(context);
        if(!isIndependent) {//if not independent - size is the same as item size
            return getRoverSize(context);
        }

        int defaultSize = getRoverDefaultSize(context);
        int scaleRatio = PrefUtils.getTriggerItemSizeValue(context);
        return (int)(defaultSize * (float)(scaleRatio/100f));
    }

}

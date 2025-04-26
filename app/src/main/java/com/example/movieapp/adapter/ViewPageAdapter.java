package com.example.movieapp.adapter;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentStatePagerAdapter;

import com.example.movieapp.fragment.Fragment_Home;
import com.example.movieapp.fragment.Fragment_Logout;
import com.example.movieapp.fragment.Fragment_Movie_Favorite;
import com.example.movieapp.fragment.Fragment_Movie_Series;
import com.example.movieapp.fragment.Fragment_Movie_Single;
import com.example.movieapp.fragment.Fragment_Movie_Type;

public class ViewPageAdapter extends FragmentStatePagerAdapter {
    public ViewPageAdapter(@NonNull FragmentManager fm, int behavior) {
        super(fm, behavior);
    }

    @NonNull
    @Override
    public Fragment getItem(int position) {
        switch (position) {
            case 0:
                return new Fragment_Home();
            case 1:
                return new Fragment_Movie_Single();
            case 2:
                return new Fragment_Movie_Series();
            case 3:
                return new Fragment_Movie_Type();
            case 4:
                return new Fragment_Movie_Favorite();
            case 5:
                return new Fragment_Logout();
            default:
                return new Fragment_Home();
        }
    }

    @Override
    public int getCount() {
        return 6;
    }
}

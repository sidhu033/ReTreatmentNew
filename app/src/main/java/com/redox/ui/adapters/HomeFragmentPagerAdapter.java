package com.redox.ui.adapters;

import android.content.Context;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.view.ViewGroup;

import com.redox.ui.fragments.DashboardFragment;
import com.redox.ui.fragments.SummaryFragment;

import static com.redox.ui.adapters.DashboardTabs.DASHBOARD;
import static com.redox.ui.adapters.DashboardTabs.SUMMARY;


public class HomeFragmentPagerAdapter extends FragmentPagerAdapter {

    Context context;
    public Fragment dashBoardFragment;

    public HomeFragmentPagerAdapter(FragmentManager fm, Context context) {
        super(fm);
        dashBoardFragment= DashboardFragment.newInstance(DASHBOARD);
        this.context = context;
    }

    @Override
    public Fragment getItem(int position) {

        switch (position) {
            case DASHBOARD:
                return dashBoardFragment;

            case SUMMARY:
                SummaryFragment summaryFragment=new SummaryFragment();
                summaryFragment.newInstance();
                return summaryFragment;

            default:
                return DashboardFragment.newInstance(position);
        }

    }




    @Override
    public int getItemPosition(Object object) {
        // POSITION_NONE makes it possible to reload the PagerAdapter

        return POSITION_NONE;
    }

    @Override
    public int getCount() {
        return 2;
    }

    @Override
    public void finishUpdate(ViewGroup container) {
        try{
            super.finishUpdate(container);
        } catch (Exception nullPointerException){
            System.out.println("Catch the NullPointerException in FragmentPagerAdapter.finishUpdate");
        }
    }
}

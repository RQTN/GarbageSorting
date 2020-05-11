package com.garbagesorting.android;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.SearchView;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.garbagesorting.android.db.Garbage;
import com.garbagesorting.android.db.History;
import com.garbagesorting.android.util.Utility;
import com.zhy.view.flowlayout.FlowLayout;
import com.zhy.view.flowlayout.TagAdapter;
import com.zhy.view.flowlayout.TagFlowLayout;

import org.litepal.crud.DataSupport;

import java.util.ArrayList;
import java.util.List;

public class SearchGarbageActivity extends AppCompatActivity {

    private static final String TAG = "SearchGarbageActivity";

    private SearchView searchView;

    private ListView listView;

    private GarbageAdapter adapter;

    private List<Garbage> suggestions = new ArrayList<>();

    private TagFlowLayout flowLayout;

    private List<String> histories = new ArrayList<>();

    private TagAdapter tagAdapter = new TagAdapter<String>(histories) {
        @Override
        public View getView(FlowLayout parent, int position, String s) {
            TextView tv = (TextView) LayoutInflater.from(SearchGarbageActivity.this).inflate(R.layout.flow_layout_tv, flowLayout, false);
            tv.setText(s);
            return tv;
        }
    };

    private LinearLayout searchHistoryLayout;

    private ImageView deleteHistory;

    private TextView cancel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search_garbage);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        ActionBar actionBar = getSupportActionBar();
        actionBar.setTitle("");

        cancel = (TextView) findViewById(R.id.cancel);
        cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        listView = (ListView) findViewById(R.id.list_view);
        adapter = new GarbageAdapter(SearchGarbageActivity.this, R.layout.garbage_item, suggestions);
        listView.setAdapter(adapter);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Garbage garbage = suggestions.get(position);


                Toast.makeText(SearchGarbageActivity.this, Utility.labelMapping(garbage.getLabel()), Toast.LENGTH_SHORT).show();
            }
        });
        listView.setVisibility(View.GONE);

        searchHistoryLayout = (LinearLayout) findViewById(R.id.search_history_layout);

        flowLayout = (TagFlowLayout) findViewById(R.id.flow_layout);
        flowLayout.setAdapter(tagAdapter);
        flowLayout.setOnTagClickListener(new TagFlowLayout.OnTagClickListener() {
            @Override
            public boolean onTagClick(View view, int position, FlowLayout parent) {
                String query = histories.get(position);
                searchView.setQuery(query, false);
                return true;
            }
        });
        List<History> tmp = DataSupport.order("id desc").find(History.class);
        if (tmp.size() == 0) {
            searchHistoryLayout.setVisibility(View.GONE);
        }
        for (History history : tmp) {
            histories.add(history.getName());
        }
        tagAdapter.notifyDataChanged();

        deleteHistory = (ImageView) findViewById(R.id.delete_history);
        deleteHistory.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                DataSupport.deleteAll(History.class);
                searchHistoryLayout.setVisibility(View.GONE);
            }
        });

    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.search_menu, menu);

        MenuItem searchItem = menu.findItem(R.id.action_search);
        searchView = (SearchView) MenuItemCompat.getActionView(searchItem);
        View searchPlate = searchView.findViewById(android.support.v7.appcompat.R.id.search_plate);
        if (searchPlate != null) {
            searchPlate.setBackgroundColor(Color.TRANSPARENT);
        }

        searchView.setIconifiedByDefault(false);
        searchView.setIconified(false);
        searchView.setQueryHint("请输入正确物品名称");
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        lp.gravity = Gravity.CENTER;

        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                Log.d(TAG, "onQueryTextSubmit = " + query);

                // 存储检索历史
                Utility.saveHistory(query);
                histories.clear();
                List<History> tmp = DataSupport.order("id desc").find(History.class);
                for (History history : tmp) {
                    histories.add(history.getName());
                }
                tagAdapter.notifyDataChanged();
                searchHistoryLayout.setVisibility(View.VISIBLE);

                if (searchView != null) {
                    InputMethodManager manager = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                    if (manager != null) {
                        manager.hideSoftInputFromWindow(searchView.getWindowToken(), 0);
                    }
                }
                searchView.clearFocus();
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                Log.d(TAG, "onQueryTextChange: " + newText);
                if (TextUtils.isEmpty(newText)) {
                    listView.setVisibility(View.GONE);
                    adapter.notifyDataSetChanged();
                } else {
                    List<Garbage> tmp = DataSupport.where("name like ?", "%" + newText + "%").find(Garbage.class);
                    suggestions.clear();
                    for (Garbage garbage : tmp) {
                        suggestions.add(garbage);
                    }
                    adapter.notifyDataSetChanged();
                    listView.setVisibility(View.VISIBLE);
                }
                return false;
            }
        });

        return super.onCreateOptionsMenu(menu);
    }
}

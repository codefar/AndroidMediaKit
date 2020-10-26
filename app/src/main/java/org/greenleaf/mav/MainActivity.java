package org.greenleaf.mav;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main3);
        ListView listView = new ListView(this);
        setContentView(listView, new ListView.LayoutParams(ListView.LayoutParams.MATCH_PARENT, ListView.LayoutParams.MATCH_PARENT));

        final ArrayAdapter<Class<Activity>> arrayAdapter = new ArrayAdapter<Class<Activity>>(this, android.R.layout.simple_list_item_1, android.R.id.text1, new Class[] {
                MainCamera1Activity.class, MainCamera2Activity.class,
                MediaMuxerActivity.class, MediaRecordActivity.class,
                MediaExtractorActivity.class
        }) {
            @NonNull
            @Override
            public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
                return super.getView(position, convertView, parent);
            }
        };
        listView.setAdapter(arrayAdapter);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                startActivity(new Intent(MainActivity.this, arrayAdapter.getItem(position)));
            }
        });
    }
}
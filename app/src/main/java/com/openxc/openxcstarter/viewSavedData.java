package com.openxc.openxcstarter;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
import com.openxcplatform.openxcstarter.R;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;

public class viewSavedData extends Activity {

    ListView listView;
    String dirPath = Environment.getExternalStorageDirectory().getPath() + "/vi-validation";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.view_saved_data);
        listView = (ListView)findViewById(R.id.listView);
        ArrayList<String> FilesInFolder = GetFiles(dirPath);

        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this,android.R.layout.simple_list_item_1, android.R.id.text1, FilesInFolder){

            @Override
            public View getView(int position, View convertView, ViewGroup parent){

                View view = super.getView(position, convertView, parent);

                TextView ListItemShow = (TextView) view.findViewById(android.R.id.text1);

                if(ListItemShow.getText().toString().contains("Uploaded"))
                {
                    ListItemShow.setTextColor(Color.parseColor("#bfbfbf"));
                }
                else {
                    ListItemShow.setTextColor(Color.parseColor("#000000"));
                }


                return view;
            }

        };

        listView.setAdapter(adapter);

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            public void onItemClick(AdapterView<?> parent, View v, final int position, long id) {

                LayoutInflater layoutInflater = LayoutInflater.from(viewSavedData.this);
                View promptView = layoutInflater.inflate(R.layout.prompt, null);

                final AlertDialog alertD = new AlertDialog.Builder(viewSavedData.this).create();
                final String filename = listView.getItemAtPosition(position).toString();
                TextView filenameDialog = (TextView) promptView.findViewById(R.id.fileName);
                filenameDialog.setText(listView.getItemAtPosition(position).toString());
                Button btnView = (Button) promptView.findViewById(R.id.btnFileView);
                Button btnUpload = (Button) promptView.findViewById(R.id.btnFileUpload);
                Button btnDelete = (Button) promptView.findViewById(R.id.btnFileDelete);

                alertD.setView(promptView);
                alertD.show();

                btnView.setOnClickListener(new View.OnClickListener() {
                    public void onClick(View v) {
                        Intent intent = new Intent(Intent.ACTION_VIEW);
                        Uri uri = Uri.parse(dirPath + "/" +  filename);
                        intent.setDataAndType(uri, "text/plain");
                        startActivity(intent);
                        alertD.dismiss();
                    }
                });

                btnUpload.setOnClickListener(new View.OnClickListener() {
                    public void onClick(View v) {
                        if(filename.contains("Uploaded"))
                        {
                            alertD.dismiss();
                            Toast.makeText(getApplicationContext(), "File already uploaded", Toast.LENGTH_SHORT).show();
                        }
                        else
                        {
                            File file = new File(dirPath + "/" +  filename);
                            //Read text from file
                            //StringBuilder text = new StringBuilder();
                            ArrayList<String> dataToPost = new ArrayList<String>();
                            try {
                                BufferedReader br = new BufferedReader(new FileReader(file));
                                String line;

                                while ((line = br.readLine()) != null) {
                                    dataToPost.add(line);
                                }
                                br.close();
                            }
                            catch (IOException e) {
                                //Need to add proper error handling
                            }
                            String[] postArray = new String[dataToPost.size()];
                            postArray = dataToPost.toArray(postArray);
                            Context mContext = getApplicationContext();
                            PostToGoogleFormTask postToGoogleFormTask = new PostToGoogleFormTask();
                            postToGoogleFormTask.myContext=mContext;
                            postToGoogleFormTask.execute(postArray);
                            File uploadedRename = new File(dirPath + "/" + "Uploaded " + filename);
                            file.renameTo(uploadedRename);
                            alertD.dismiss();
                            finish();
                            startActivity(getIntent());
                        }
                    }
                });

                btnDelete.setOnClickListener(new View.OnClickListener() {
                    public void onClick(View v) {
                        File file = new File(dirPath + "/" +  filename);
                        file.delete();
                        alertD.dismiss();
                        finish();
                        startActivity(getIntent());
                    }
                });
            }
        });
    }

    public ArrayList<String> GetFiles(String DirectoryPath) {
        ArrayList<String> MyFiles = new ArrayList<String>();
        File f = new File(DirectoryPath);

        if(f.canRead())
        {
            f.mkdirs();
            File[] files = f.listFiles();
            if (files.length == 0)
                return null;
            else {
                for (File file : files) MyFiles.add(file.getName());
            }
        }
        return MyFiles;
    }

}

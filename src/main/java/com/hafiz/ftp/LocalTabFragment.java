package com.hafiz.ftp;

import android.content.Context;
import android.os.Bundle;
import android.os.Environment;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;

import org.apache.commons.net.ftp.FTPFile;

import java.io.File;
import java.util.ArrayList;

public class LocalTabFragment extends Fragment {

    EditText addressBar;
    String workingDirectory = null;
    Context context = getContext();
    File file;

    public void setAddressBarText(String path) {
        addressBar.setText(path);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_layout, container, false);

        addressBar = (EditText) view.findViewById(R.id.address_bar);
        Log.d("before", "before working directory");
        if(workingDirectory == null) {
            try {
                file = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);
            } catch (Exception e) {
                file = context.getFilesDir();
            }

            workingDirectory = file.getPath();
        }
        Log.d("local working directory", workingDirectory);

        ArrayList<String> filenamesList = new ArrayList();

        try {
            if (file.isDirectory()) {
                File[] files = file.listFiles();

                for (File f : files) {
                    filenamesList.add(f.getName());
                    Log.d("file", f.getName());
                }



                setAddressBarText(workingDirectory);

                String[] filenames = new String[filenamesList.size()];
                filenames = filenamesList.toArray(filenames);
                Log.d("filenames", filenames.toString());
                for (String filename: filenames){
                    Log.d("Filename", filename);
                }
                ListCustomAdapter adapter = new ListCustomAdapter(getActivity(), filenames);

                ListView listView = (ListView) view.findViewById(R.id.listview);
                listView.setAdapter(adapter);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return view;
    }
}
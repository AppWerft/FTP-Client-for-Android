package com.hafiz.ftp;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Environment;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

import org.apache.commons.net.ftp.FTPFile;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class RemoteTabFragment extends Fragment implements FTPResponse, RenameDialogFragment.RenameDialogListener {

    //contains site/connection record
    Map<String, String> site;

    Bundle args;
    Context context;
    View view;

    //contains a view to to populate and get its text
    EditText addressBar;

    //to delegate work to this activity after async task
    RemoteTabFragment taskDelegate = this;

    // fileMap and filenamesList will contain loaded files info.
    HashMap<String, FTPFile> fileMap = new HashMap<>();
    ArrayList<String> filenamesList = new ArrayList();
    ArrayList<Boolean> deleteablesList = new ArrayList();

    public void onDialogPositiveClick(RenameDialogFragment dialogFragment, String filename, EditText rename) {
        Bundle args = new Bundle();
        args.putString("filename", filename);
        args.putString("rename", rename.getText().toString());
        Log.d("bundle",args.toString());
        startFtpTask("rename", addressBar.getText().toString(), args);
        dialogFragment.getDialog().cancel();
    }

    public void setAddressBarText(String path) {
        TabActivity parentActivity = (TabActivity) getActivity();
        parentActivity.remoteAddress = path;
        addressBar.setText(path);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        args = getArguments();
    }

    public void setSite(Map<String, String> msite) {
        site = msite;
    }

    /**
     * Get sitename from arg, get site data from db and connect to site via FTP Task or
     * just list if files already exist
     *
     * @param inflater
     * @param container
     * @param savedInstanceState
     * @return view
     */
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        context = getContext();

        view = inflater.inflate(R.layout.fragment_layout, container, false);
        addressBar = (EditText) view.findViewById(R.id.address_bar);

        String sitename = args.getString("sitename");
        DatabaseHandler dbHandler = new DatabaseHandler(context);
        site = dbHandler.getSite(sitename);

        if (filenamesList.size() > 0) {
            setupListViewAdapter();
        } else {
            startFtpTask("list", "/");
        }

        return view;
    }

    /**
     * Implement FTPResponse interface method, which process ftp list response and setup
     * listview adapter and addressbar
     *
     * @param output
     * @param files
     * @param workingDirectory
     */
    public void processListResponse(String output, FTPFile[] files, String workingDirectory) {

        filenamesList.clear();
        deleteablesList.clear();
        for (FTPFile file : files) {
            fileMap.put(file.getName(), file);
            filenamesList.add(file.getName());
            Boolean isEmpty = (file.getSize() == 0);
            deleteablesList.add((!file.isDirectory() || isEmpty) );
        }

        setupListViewAdapter();

        setAddressBarText(workingDirectory);

        Toast.makeText(context, "Loaded", Toast.LENGTH_LONG).show();
    }


    /**
     * Setup listViewAdapter and set item click listeners for listview
     */
    public void setupListViewAdapter() {
        String[] filenames = new String[filenamesList.size()];
        filenames = filenamesList.toArray(filenames);

        Boolean[] deletables = new Boolean[filenamesList.size()];
        deletables = deleteablesList.toArray(deletables);

        ListCustomAdapter adapter = new ListCustomAdapter(getActivity(), filenames, this, deletables, fileMap);

        ListView listView = (ListView) view.findViewById(R.id.listview);
        listView.setAdapter(adapter);

        listView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
                RenameDialogFragment dialog = new RenameDialogFragment();
                dialog.mListener = RemoteTabFragment.this;

                Bundle b = new Bundle();
                b.putString("filename", (String) parent.getItemAtPosition(position));
                dialog.setArguments(b);
                dialog.show(getActivity().getFragmentManager(), "RenameDialogFragment");
                return true;
            }
        });

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Log.d("onItemClick", "" + position);
                // When clicked, show a toast with the TextView text
                String filename = (String) parent.getItemAtPosition(position);
                Log.d(filename, filename);

                if (fileMap.get(filename).isDirectory()) {
                    if (filename == ".") {
                        Toast.makeText(getContext(), "Reloading list .. ", Toast.LENGTH_LONG).show();
                    } else {
                        Toast.makeText(getContext(), "Loading: " + filename, Toast.LENGTH_LONG).show();
                    }
                    changeDirectory(filename);
                } else {
                    Toast.makeText(getContext(), "Downloading: " + filename, Toast.LENGTH_LONG).show();

                    Bundle args = new Bundle();
                    args.putString("filename", filename);

                    TabActivity parentActivity = (TabActivity) getActivity();
                    File localfile = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);

                    String address = localfile.getPath();
                    args.putString("localPath", address);
                    Log.d("bundle", args.toString());
                    startFtpTask("download", addressBar.getText().toString(), args);
                }
            }
        });
    }

    /**
     * Change Directory by setting Async Ftp Task
     * @param filename
     */
    public void changeDirectory(String filename) {
        String appendablePath = addressBar.getText().toString();

        if (appendablePath == "/") {
            appendablePath = "";
        }

        String directoryPath = (appendablePath + "/" + filename);

        startFtpTask("list", directoryPath);

        filenamesList.clear();
        fileMap.clear();
    }

    public void startFtpTask(String operation,String directoryPath) {
        FtpTask task = new FtpTask(context, site, operation, directoryPath);
        task.delegate = taskDelegate;
        task.execute();
    }


    public void startFtpTask(String operation, String directoryPath, Bundle arguments) {
        FtpTask task = new FtpTask(context, site, operation, directoryPath, arguments);
        task.delegate = taskDelegate;
        task.execute();
    }

    public void processUploadResponse(String output) {

    }

    public void processDownloadResponse(String output) {
        Toast.makeText(getContext(), "File downloaded" , Toast.LENGTH_LONG).show();
    }

    public void processDeleteResponse(String output) {
        Toast.makeText(getContext(), "Deleted" , Toast.LENGTH_LONG).show();
        String directoryPath = addressBar.getText().toString();
        startFtpTask("list", directoryPath);
    }

    public void handleException(Exception exception) {
        Intent intent = new Intent(this.getActivity(), MainActivity.class);
        intent.putExtra("message", exception.toString());
        startActivity(intent);
    }

    public void invalidConnectionError(String message){
        Intent intent = new Intent(this.getActivity(), MainActivity.class);
        intent.putExtra("message", message);
        startActivity(intent);

    }


}
package com.example.uninstallapk;

import android.app.ProgressDialog;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    LinearLayout linearLayout;

    Button button,cancel,noSilent;

    List<Integer> pages=new ArrayList<>();
    List<Integer> pagesCancels=new ArrayList<>();
    List<View> views=new ArrayList<>();
    List<CheckBox> checkBoxes=new ArrayList<>();

    int number=0;

    ProgressDialog progressDialog;

    List<PackageInfo> packageInfos=new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        linearLayout = (LinearLayout) findViewById(R.id.linear1);

        button=(Button) findViewById(R.id.start_delete);
        cancel=(Button) findViewById(R.id.cancel_choice);
        noSilent=(Button) findViewById(R.id.no_silent);

        PackageManager packageManager=getPackageManager();
        packageInfos=packageManager.getInstalledPackages(PackageManager.GET_UNINSTALLED_PACKAGES);

        int id=0;
        for (PackageInfo packageInfo:packageInfos){
            String str=packageInfo.applicationInfo.loadLabel(getPackageManager()).toString();
            linearLayout.addView(getChoiceView(linearLayout,str,id));
            id++;
        }

        button.setOnClickListener(this);
        cancel.setOnClickListener(this);
        noSilent.setOnClickListener(this);
    }

    private View getChoiceView(LinearLayout root, final String pageName, int id){
        final View view = LayoutInflater.from(this).inflate(R.layout.choice_layout, root, false);

        final CheckBox checkBox=(CheckBox) view.findViewById(R.id.page_id);
        final TextView textView=(TextView) view.findViewById(R.id.page_name);

        view.setTag(id);

        checkBox.setTag(view);

        checkBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {

                if (isChecked){

                    views.add((View) checkBox.getTag());
                    pages.add((int) view.getTag());
                    pagesCancels.add((int) view.getTag());

                }else {

                    View view1=(View) checkBox.getTag();
                    views.remove(view1);
                    pages.remove(getIndexPages((int) view1.getTag()));

                }

            }
        });

        checkBoxes.add(checkBox);

        textView.setText(pageName);

        return view;
    }

    public int getIndexPages(int id){
        int index=0;
        int j=0;

        for (int i:pages){
            if (i==id){
                index=j;
                break;
            }
            j++;
        }

        return index;
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.start_delete:
                new DEleteApk().execute();
                break;
            case R.id.cancel_choice:

                for (int i:pagesCancels){
                    checkBoxes.get(i).setChecked(false);
                }

                pagesCancels.removeAll(pagesCancels);
                break;
            case R.id.no_silent:

                for (int i:pages){
                    unstallApp(packageInfos.get(i).packageName);
                    number++;
                }
                break;
        }
    }


    public void unstallApp(String pageName){
        Intent uninstall_intent = new Intent();
        uninstall_intent.setAction(Intent.ACTION_DELETE);
        uninstall_intent.setData(Uri.parse("package:"+pageName));
        startActivityForResult(uninstall_intent,1);

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode==1){
            if (resultCode==0){
                if (pages.size()==number){

                    pages.removeAll(pages);
                    number=0;

                    for (View view:views){
                        linearLayout.removeView(view);
                    }
                    views.removeAll(views);

                }
            }
        }
    }

    class DEleteApk extends AsyncTask {

        @Override
        protected void onPreExecute() {
            progressDialog=new ProgressDialog(MainActivity.this);
            progressDialog.setTitle("正在卸载中");
            progressDialog.setMessage("请稍后...");
            progressDialog.setCancelable(true);
            progressDialog.show();
        }

        @Override
        protected Object doInBackground(Object[] objects) {
            for (int id:pages){
                RootCmd.unInstallApk(packageInfos.get(id).packageName);
            }
            return true;
        }

        @Override
        protected void onPostExecute(Object o) {
            progressDialog.dismiss();

            pages.removeAll(pages);
            for (View view:views){
                linearLayout.removeView(view);
            }
            views.removeAll(views);
        }
    }
}

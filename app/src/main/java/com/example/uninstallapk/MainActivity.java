package com.example.uninstallapk;

import android.app.Activity;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    LinearLayout linearLayout;

    Button button,cancel,noSilent,search;

    List<Integer> pages=new ArrayList<>();
    List<Integer> pagesCancels=new ArrayList<>();
    List<View> views=new ArrayList<>();
    List<CheckBox> checkBoxes=new ArrayList<>();

    Dialog dialog;
    EditText inContent;

    int number=0;

    ProgressDialog progressDialog;

    List<PackageInfo> packageInfos=new ArrayList<>();
    List<PackageModel> packageSearchs=new ArrayList<>();
    List<PackageModel> packageModels=new ArrayList<>();

    boolean isBack=false;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        linearLayout = (LinearLayout) findViewById(R.id.linear1);

        button=(Button) findViewById(R.id.start_delete);
        cancel=(Button) findViewById(R.id.cancel_choice);
        noSilent=(Button) findViewById(R.id.no_silent);
        search=(Button) findViewById(R.id.search_id);

        PackageManager packageManager=getPackageManager();
        packageInfos=packageManager.getInstalledPackages(PackageManager.GET_UNINSTALLED_PACKAGES);

        addAllView();

        button.setOnClickListener(this);
        cancel.setOnClickListener(this);
        noSilent.setOnClickListener(this);
        search.setOnClickListener(this);
    }

    public void addAllView(){
        int id=0;
        for (PackageInfo packageInfo:packageInfos){
            PackageModel packageModel=new PackageModel(packageInfo.packageName,packageInfo.applicationInfo.loadLabel(getPackageManager()).toString(),id);
            packageModels.add(packageModel);

            String str=packageInfo.applicationInfo.loadLabel(getPackageManager()).toString();
            linearLayout.addView(getChoiceView(linearLayout,str,id));
            id++;
        }
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
            case R.id.search_id:
                if (isBack){
                   if (pages.size()>0){
                       pages.removeAll(pages);
                   }
                   if (views.size()>0){
                       views.removeAll(views);
                   }

                   linearLayout.removeAllViews();

                    packageModels.removeAll(packageModels);
                   addAllView();

                   isBack=false;
                   search.setText("搜索");
                }else {
                    showSearchDialog();
                }

                break;
            default:break;
        }
    }

    private void showSearchDialog(){
        dialog=new Dialog(this);

        final View inflate = LayoutInflater.from(this).inflate(R.layout.in_content, null);
        final Button determineButton=(Button)inflate.findViewById(R.id.determine_button);

        TextView titleContent=(TextView)inflate.findViewById(R.id.title_content);
        inContent=(EditText)inflate.findViewById(R.id.in_content);

        titleContent.setText("搜索");
        determineButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new SearchApk().execute();
            }
        });


        dialog.setContentView(inflate);

        Window dialogWindow = dialog.getWindow();
        dialogWindow.setGravity( Gravity.CENTER);
        WindowManager.LayoutParams lp = dialogWindow.getAttributes();

        lp.y = 20;
        dialogWindow.setAttributes(lp);
        dialog.show();//显示对话框
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


                    number=0;

                    for (int id:pages){
                        packageInfos.remove(id);
                    }
                    for (View view:views){
                        linearLayout.removeView(view);
                    }
                    views.removeAll(views);
                    pages.removeAll(pages);
                    packageModels.removeAll(packageModels);

                    addAllView();
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
                RootCmd.unInstallApk(packageModels.get(id).getPackageName());
            }
            return true;
        }

        @Override
        protected void onPostExecute(Object o) {
            progressDialog.dismiss();

            for (int id:pages){
                packageInfos.remove(id);
            }
            for (View view:views){
                linearLayout.removeView(view);
            }
            views.removeAll(views);
            pages.removeAll(pages);
            packageModels.removeAll(packageModels);

            addAllView();
        }
    }

    class SearchApk extends AsyncTask {

        @Override
        protected void onPreExecute() {
            dialog.dismiss();

            progressDialog=new ProgressDialog(MainActivity.this);
            progressDialog.setTitle("正在搜索中");
            progressDialog.setMessage("请稍后...");
            progressDialog.setCancelable(true);
            progressDialog.show();
        }

        @Override
        protected Object doInBackground(Object[] objects) {

            if (packageSearchs.size()>0){
                packageSearchs.removeAll(packageSearchs);
            }

            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

            String str=inContent.getText().toString();

            for (PackageModel packageModel:packageModels){
                if (packageModel.getPackageLabel().indexOf(str)!=-1){
                    packageSearchs.add(packageModel);
                }
            }
            return true;
        }

        @Override
        protected void onPostExecute(Object o) {
            progressDialog.dismiss();

            search.setText("返回");
            isBack=true;

            if (packageSearchs.size()>0){
                if (pages.size()>0){
                    pages.removeAll(pages);
                }
                if (views.size()>0){
                    views.removeAll(views);
                }

                linearLayout.removeAllViews();

                for (PackageModel packageModel:packageSearchs){
                    linearLayout.addView(getChoiceView(linearLayout,packageModel.getPackageLabel(),packageModel.getId()));
                }
            }else {
                Toast.makeText(getApplicationContext(),"未找到该应用",Toast.LENGTH_SHORT).show();
            }


        }
    }
}

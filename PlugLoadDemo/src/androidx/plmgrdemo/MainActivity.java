package androidx.plmgrdemo;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Collection;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.os.Bundle;
import android.os.Environment;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.Toast;
import androidx.pluginmgr.PlugInfo;
import androidx.pluginmgr.PluginManager;

public class MainActivity extends Activity {
	// private EditText pluginDirTxt;
	// private Button pluginLoader;
	private ListView pluglistView;
	private PluginManager plugMgr;
	private Context mContext;
	private Boolean plugLoading=false;


	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		mContext=this;
		setContentView(R.layout.activity_main);

		Button btn = (Button) findViewById(R.id.btn1);
		btn.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {

			}
		});
		pluglistView = (ListView) findViewById(R.id.pluglist);
		pluglistView.setOnItemClickListener(new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> parent, View view,
									int position, long id) {
				plugItemClick(position);
			}
		});

		copyApkFile();
		plugMgr = PluginManager.getInstance(this);
		loadApk();

	}

	private void copyApkFile(){
		File file=mContext.getFilesDir();
		File apksFile=new File(file,"apks");
		if(!apksFile.exists()){
			apksFile.mkdir();
		}

		try {
			String[] apks=this.getAssets().list("apks");
			if(apks==null){
				return;
			}
			for (String str:apks) {
				Log.e("lmf", ">>>>>>>apks>>>>"+str);
				File temp=new File(apksFile,str);
				if(!temp.exists()){
					copyData("apks/"+str,temp);
				}
			}


		} catch (IOException e) {
			e.printStackTrace();
		}

	}

	private void copyData(String source,File file) throws IOException
	{
		InputStream myInput;
		OutputStream myOutput = new FileOutputStream(file);
		myInput = this.getAssets().open(source);
		byte[] buffer = new byte[1024];
		int length = myInput.read(buffer);
		while(length > 0)
		{
			myOutput.write(buffer, 0, length);
			length = myInput.read(buffer);
		}

		myOutput.flush();
		myInput.close();
		myOutput.close();
	}


	private void loadApk(){
		final String dirText =mContext.getFilesDir().getAbsolutePath()+"/apks/";

		if (plugLoading) {
			Toast.makeText(mContext, getString(R.string.loading),
					Toast.LENGTH_LONG).show();
			return;
		}
		String strDialogTitle = getString(R.string.dialod_loading_title);
		String strDialogBody = getString(R.string.dialod_loading_body);
		final ProgressDialog dialogLoading = ProgressDialog.show(
				mContext, strDialogTitle, strDialogBody, true);
		new Thread(new Runnable() {
			@Override
			public void run() {
				plugLoading = true;
				try {
					Collection<PlugInfo> plugs = plugMgr
							.loadPlugin(new File(dirText));
					setPlugins(plugs);
				} catch (Exception e) {
					e.printStackTrace();
				} finally {
					dialogLoading.dismiss();
				}
				plugLoading = false;
			}
		}).start();
	}

	private void plugItemClick(int position) {
		PlugInfo plug = (PlugInfo) pluglistView.getItemAtPosition(position);
		plugMgr.startMainActivity(this, plug.getPackageName());
	}

	private void setPlugins(final Collection<PlugInfo> plugs) {
		if (plugs == null || plugs.isEmpty()) {
			return;
		}
		final ListAdapter adapter = new PlugListViewAdapter(this, plugs);
		runOnUiThread(new Runnable() {
			public void run() {
				pluglistView.setAdapter(adapter);
			}
		});
	}
}

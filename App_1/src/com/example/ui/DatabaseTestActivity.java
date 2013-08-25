package com.example.ui;

import java.util.List;
import java.util.Random;

import android.app.ListActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;

import com.example.app_1.R;
import com.example.models.MyImageObject;
import com.example.utils.MyDBAdapter;

public class DatabaseTestActivity extends ListActivity {
	private MyDBAdapter mda;
	
	 @Override
	  public void onCreate(Bundle savedInstanceState) {
	    super.onCreate(savedInstanceState);
	    setContentView(R.layout.activity_test_db);

	    mda = MyDBAdapter.getInstance();
	    mda.open();

	    List<MyImageObject> values = mda.getAllImages();

	    // Use the SimpleCursorAdapter to show the
	    // elements in a ListView
	    ArrayAdapter<MyImageObject> adapter = new ArrayAdapter<MyImageObject>(this, android.R.layout.simple_list_item_1, values);
	    setListAdapter(adapter);
	  }

	  // Will be called via the onClick attribute
	  // of the buttons in main.xml
	  public void onClick(View view) {
	    @SuppressWarnings("unchecked")
	    ArrayAdapter<MyImageObject> adapter = (ArrayAdapter<MyImageObject>) getListAdapter();
	    MyImageObject mio = null;
	    switch (view.getId()) {
	    case R.id.add:
	      String[] mios = new String[] { "Cool", "Very nice", "Hate it" };
	      int nextInt = new Random().nextInt(3);
	      MyImageObject tmp_mio = new MyImageObject(mios[nextInt]);
	      // Save the new comment to the database
	      adapter.add(mda.insertImage(tmp_mio));

	      break;
	    case R.id.delete:
	      if (getListAdapter().getCount() > 0) {
	        mio = (MyImageObject) getListAdapter().getItem(0);
	        mda.deleteImage(mio);
	        adapter.remove(mio);
	      }
	      break;
	    }
	    adapter.notifyDataSetChanged();
	  }

	  @Override
	  protected void onResume() {
	    mda.open();
	    super.onResume();
	  }

	  @Override
	  protected void onPause() {
	    mda.close();
	    super.onPause();
	  }
}

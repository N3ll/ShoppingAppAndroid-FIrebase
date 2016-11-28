package org.projects.shoppinglist;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Paint;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.ShareActionProvider;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.firebase.ui.database.FirebaseListAdapter;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.remoteconfig.FirebaseRemoteConfig;

import java.util.HashMap;
import java.util.Map;

public class MainActivity extends AppCompatActivity implements DialogCloseListener {
    private static final String TAG = "EmailPassword";

    ListView listView;
    String product;
    String number;

    static MyDialogFragment dialog;
    static Context context;

    ProductsInfo lastDeletedProduct;
    int lastDeletedPosition;

    DatabaseReference ref;
    public FirebaseListAdapter<ProductsInfo> pAdapter;

    FirebaseRemoteConfig mFirebaseRemoteConfig = FirebaseRemoteConfig.getInstance();

    private FirebaseAuth mAuth;
    private FirebaseAuth.AuthStateListener mAuthListener;
    FirebaseUser user;

    private ShareActionProvider shareActionProvider;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        this.context = this;

        //setting personal preferences if any
        this.setPersonalPref();


        //checking whether there is a logged user and if not redirect to AuthActivity
        this.user = FirebaseAuth.getInstance().getCurrentUser();
        if (user == null) {
            System.out.println("The user is null, start new activity");
            Intent intent = new Intent(context, AuthActivity.class);
            context.startActivity(intent);
        } else {
            String currentUserUid = user.getUid();
            ref = FirebaseDatabase.getInstance().getReference().child(currentUserUid);
        }


        //change the app name from the Firebase console
        Map<String, Object> defaults = new HashMap<>();
        defaults.put("app_name", getResources().getString(R.string.app_name));
        mFirebaseRemoteConfig.setDefaults(defaults);
        Task<Void> myTask = mFirebaseRemoteConfig.fetch(1);
        myTask.addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if (task.isSuccessful()) {
                    mFirebaseRemoteConfig.activateFetched();
                    String name = mFirebaseRemoteConfig.getString("app_name");
                    getSupportActionBar().setTitle(name);
                } else
                    Log.d("ERROR", "Task not succesfull + " + task.getException());
            }
        });

        //setting Firebase.AuthStateListener ro handle changes when logging out
        mAuth = FirebaseAuth.getInstance();
        mAuthListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                user = firebaseAuth.getCurrentUser();
                if (user != null) {
                    // User is signed in
                    Log.d(TAG, "onAuthStateChanged:signed_in:" + user.getUid());
                } else {
                    // User is signed out
                    Log.d(TAG, "onAuthStateChanged:signed_out");
                    Intent intent = new Intent(MainActivity.this, AuthActivity.class);
                    MainActivity.this.startActivity(intent);
                }
            }
        };

        //initializing the listview  and FirebaseAdapter
        if (ref != null) {
            pAdapter = new FirebaseListAdapter<ProductsInfo>(this, ProductsInfo.class, R.layout.product, ref) {
                @Override
                protected void populateView(final View view, final ProductsInfo product, final int position) {
                    System.out.println("populate view. products is " + product);
                    if (product != null) {
                        final CheckBox productChecked = (CheckBox) view.findViewById(R.id.product_checked);
                        productChecked.setText(product.getName() + " " + product.getNumber());

                        productChecked.setTag(position);

//                        productChecked.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
//                            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
//                                int getPosition = (Integer) buttonView.getTag();
//                                pAdapter.getRef(getPosition).child("isCrossed").setValue(isChecked);
//                                System.out.println("onCheckedListener triggered ");
//                            }
//                        });

                        productChecked.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                int getPosition = (Integer) v.getTag();
                                CheckBox chB = (CheckBox)v;
                                pAdapter.getRef(getPosition).child("isCrossed").setValue(chB.isChecked());
                                System.out.println("onCheckedListener triggered ");
                            }
                        });

                        if (product.getIsCrossed()) {
                            productChecked.setChecked(true);
                            productChecked.setPaintFlags(productChecked.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);
                            System.out.println("Product crossed tag " + productChecked.getTag());
                        } else {
                            productChecked.setChecked(false);
                            productChecked.setPaintFlags(productChecked.getPaintFlags() & (~Paint.STRIKE_THRU_TEXT_FLAG));
                            System.out.println("Product NOT crossed tag " + productChecked.getTag());

                        }

                        final ImageButton btn = (ImageButton) view.findViewById(R.id.btnDel);
                        btn.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                lastDeletedPosition = position;
                                lastDeletedProduct = pAdapter.getItem(position);
                                pAdapter.getRef(lastDeletedPosition).removeValue();
                                System.out.println("Item set to null");
                                pAdapter.notifyDataSetChanged();


                                System.out.println("Parent " + view.getParent());
                                Snackbar snackbar = Snackbar
                                        .make((View) view.getParent(), "Item Deleted", Snackbar.LENGTH_LONG)
                                        .setAction("UNDO", new View.OnClickListener() {
                                            @Override
                                            public void onClick(View view) {
                                                ref.push().setValue(lastDeletedProduct);
                                                pAdapter.notifyDataSetChanged();

                                                System.out.println("Product " + lastDeletedProduct.toString());
                                                System.out.println("Position " + lastDeletedPosition);
                                                System.out.println("Parent " + (View) view.getParent());

                                                Snackbar snackbar = Snackbar.make((View) view.getParent(), "Item restored at the end of the list!", Snackbar.LENGTH_SHORT);
                                                snackbar.show();
                                            }
                                        });

                                snackbar.show();
                            }
                        });
                    }
                }
            };
        }
        listView = (ListView) findViewById(R.id.list);
        listView.setAdapter(pAdapter);

        //initializing the spinner
        final Spinner spinner = (Spinner) findViewById(R.id.spinner1);
        Button addButton = (Button) findViewById(R.id.addButton);
        addButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                EditText addField = (EditText) findViewById(R.id.addField);
                product = addField.getText().toString();

                ProductsInfo p = new ProductsInfo(product, number, false);
                addField.setText("");
                spinner.setSelection(0);

                ref.push().setValue(p);
                pAdapter.notifyDataSetChanged();
            }
        });


        final ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this, R.array.numbers,
                android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(adapter);

        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {

            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view,
                                       int position, long id) {
                number = (String) spinner.getSelectedItem();

            }

            @Override
            public void onNothingSelected(AdapterView<?> arg0) {
                number = "0";
            }
        });

    }

    @Override
    public void onStart() {
        super.onStart();
        mAuth.addAuthStateListener(mAuthListener);
    }

    //This method is called before our activity is created
    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        EditText addField = (EditText) findViewById(R.id.addField);
        outState.putString("product", addField.getText().toString());

        Spinner addNumber = (Spinner) findViewById(R.id.spinner1);
        outState.putInt("spinner", addNumber.getSelectedItemPosition());
    }

    //this is called when our activity is recreated, but
    //AFTER our onCreate method has been called
    //EXTREMELY IMPORTANT DETAIL
    @Override
    protected void onRestoreInstanceState(Bundle savedState) {
        Log.i("log", "in onRestoreInstanceState");
        super.onRestoreInstanceState(savedState);
        EditText addProduct = (EditText) findViewById(R.id.addField);
        this.product = savedState.getString("product");
        addProduct.setText(this.product);

        Spinner addNumber = (Spinner) findViewById(R.id.spinner1);
        addNumber.setSelection(savedState.getInt("spinner"));

        pAdapter.notifyDataSetChanged();
    }


    public void clearShoppingList() {
        dialog = new MyDialog();
        Bundle bundle = new Bundle();
        bundle.putString("userUid", user.getUid());
        dialog.setArguments(bundle);
        dialog.show(getFragmentManager(), "MyFragment");
    }

    public void handleDialogClose(DialogInterface dialog) {
        pAdapter.notifyDataSetChanged();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        MenuItem menuItem = menu.findItem(R.id.action_share);
        shareActionProvider = (ShareActionProvider) MenuItemCompat.getActionProvider(menuItem);

        if (pAdapter != null) {
            String list = "What is left to buy: \n";
            for (int i = 0; i < pAdapter.getCount(); i++) {
                ProductsInfo p = pAdapter.getItem(i);
                if (!p.getIsCrossed()) {
                    list += p.toString() + "\n";
                }
            }
            System.out.println("The list " + list);
            setIntent(list);
        }

        return true;
    }

    private void setIntent(String text) {
        Intent intent = new Intent(Intent.ACTION_SEND);
        intent.setType("text/plain");
        intent.putExtra(Intent.EXTRA_TEXT, text);
        shareActionProvider.setShareIntent(intent);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        switch (item.getItemId()) {
            case R.id.action_settings:
                Intent intent = new Intent(this, SettingsActivity.class);
                startActivityForResult(intent, 1);
                return true; //return true, means we have handled the event
            case R.id.clear_all:
                clearShoppingList();
                return true;
            case R.id.logout:
                System.out.println(ref.getRoot().toString());
                mAuth.signOut();
                return true;
        }

        return false;
    }

    public void setPersonalPref() {
        boolean isPersonal = MyPreferenceFragment.isPersonalized(this);
        String name = MyPreferenceFragment.getName(this);
        TextView msg = (TextView) findViewById(R.id.msg);

        if (isPersonal) {
            if (name.equals("")) {
                msg.setText("Welcome to the shopping list of the person with no name");
            } else {
                msg.setText("Welcome to " + name + "'s shopping list");
            }
        } else {
            msg.setText("Welcome to my shopping list");
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == 1) //from settings
        {
            this.setPersonalPref();
        }
        super.onActivityResult(requestCode, resultCode, data);
    }


    public static class MyDialog extends MyDialogFragment {


        @Override
        protected void positiveClick() {
            String userUid = getArguments().getString("userUid");
            DatabaseReference ref = FirebaseDatabase.getInstance().getReference().child(userUid);
            ref.removeValue();

            Toast toast = Toast.makeText(context,
                    "Your list was deleted!" +
                            "" +
                            "", Toast.LENGTH_SHORT);
            toast.show();
        }

        @Override
        protected void negativeClick() {
            Toast toast = Toast.makeText(context,
                    "Phiu! Your list is save", Toast.LENGTH_SHORT);
            toast.show();
        }
    }

}

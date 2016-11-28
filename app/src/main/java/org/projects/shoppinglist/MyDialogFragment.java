package org.projects.shoppinglist;

/**
 * Created by Nelly on 10/6/16.
 */

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.os.Bundle;


public class MyDialogFragment extends DialogFragment {
    public MyDialogFragment() {

    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {

        //Here we create a new dialogbuilder;
        AlertDialog.Builder alert = new AlertDialog.Builder(
                getActivity());
        alert.setTitle("Confirmation");
        alert.setMessage("Are you sure you want to delete you shopping list?");
        alert.setPositiveButton("Yes", pListener);
        alert.setNegativeButton("No", nListener);

        return alert.create();
    }

    //This is our positive listener for when the user presses
    //the yes button
    DialogInterface.OnClickListener pListener = new DialogInterface.OnClickListener() {

        @Override
        public void onClick(DialogInterface arg0, int arg1) {
            // these will be executed when user click Yes button
            positiveClick();
            Activity activity = getActivity();
            if (activity instanceof DialogCloseListener)
                ((DialogCloseListener) activity).handleDialogClose(arg0);
            System.out.println("in positive listener method");
        }
    };

    //This is our negative listener for when the user presses
    //the no button.
    DialogInterface.OnClickListener nListener = new DialogInterface.OnClickListener() {

        @Override
        public void onClick(DialogInterface arg0, int arg1) {
            // these will be executed when user click No button
            negativeClick();

        }
    };

//    DialogInterface.OnDismissListener dListerner = new DialogInterface.OnDismissListener() {
//        @Override
//        public void onDismiss(DialogInterface dialog) {
//            Activity activity = getActivity();
//            if (activity instanceof DialogCloseListener)
//                ((DialogCloseListener) activity).handleDialogClose(dialog);
//            System.out.println("hello");
//        }
//    };

    //These two methods are empty, because they will
    //be overridden
    protected void positiveClick() {

    }

    protected void negativeClick() {

    }
}

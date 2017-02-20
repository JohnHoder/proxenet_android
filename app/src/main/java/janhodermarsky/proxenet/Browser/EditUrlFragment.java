package janhodermarsky.proxenet.Browser;

import android.annotation.TargetApi;
import android.graphics.drawable.ColorDrawable;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.TextView.OnEditorActionListener;

import janhodermarsky.proxenet.R;

public class EditUrlFragment extends DialogFragment implements
        OnEditorActionListener {

	public interface EditUrlDialogListener {
		void onFinishEditDialog(String inputText);
	}

    private EditUrlDialogListener mListener;

	private EditText mEditText;
    private Button btnOK;
    private Button btnCancel;

    private String editedUrl;

	public EditUrlFragment() {
	}

    public static EditUrlFragment newInstance(String url){

        EditUrlFragment dialogFragment = new EditUrlFragment();
        Bundle bundle = new Bundle();
        bundle.putString("URL", url);
        dialogFragment.setArguments(bundle);

        return dialogFragment;

    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        try {
            mListener = (EditUrlDialogListener) getTargetFragment();
        } catch (Exception e) {
            throw new ClassCastException("Calling Fragment must implement OnAddFriendListener");
        }
    }

    @TargetApi(Build.VERSION_CODES.HONEYCOMB_MR2)
    @Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {

        String urlInit = getArguments().getString("URL");

		View view = inflater.inflate(R.layout.frag_edit_url, container);
		mEditText = (EditText) view.findViewById(R.id.edittext_url);
		getDialog().setTitle("Edit url");
        getDialog().getWindow().requestFeature(Window.FEATURE_NO_TITLE);

        /*DisplayMetrics metrics = getResources().getDisplayMetrics();
        int width = metrics.widthPixels;
        getDialog().getWindow().setLayout((6 * width)/7, ViewGroup.LayoutParams.WRAP_CONTENT);*/


        /*WindowManager.LayoutParams lp = new WindowManager.LayoutParams();
        Window window = getDialog().getWindow();
        lp.copyFrom(window.getAttributes());
        Display display = getActivity().getWindowManager().getDefaultDisplay();
        Point size = new Point();
        display.getSize(size);
        lp.width = (size.x/3);
        lp.height = (size.y/3);
        Log.e(size.x+"::::::", size.y+"");
        window.setAttributes(lp);*/

        getDialog().getWindow().setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));

		// Show soft keyboard automatically
		mEditText.requestFocus();
		mEditText.setOnEditorActionListener(this);
        mEditText.setText(urlInit);

        btnOK = (Button) view.findViewById(R.id.OKbtn);
        btnOK.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                editedUrl = mEditText.getText().toString();
                mListener.onFinishEditDialog(editedUrl);

                getDialog().dismiss();
            }
        });

        btnCancel = (Button) view.findViewById(R.id.cancelBtn);
        btnCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                getDialog().dismiss();
            }
        });

		return view;
	}

	@Override
	public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
		if (EditorInfo.IME_ACTION_DONE == actionId) {
			EditUrlDialogListener activity = (EditUrlDialogListener) getTargetFragment();
			activity.onFinishEditDialog(mEditText.getText().toString());
			this.dismiss();
			return true;
		}
		return false;
	}
}
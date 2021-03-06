package com.sadna.utils;

import java.util.ArrayList;
import java.util.List;

import com.sadna.enums.ItemState;
import com.sadna.interfaces.IWidgetItemInfo;
import com.sadna.widgets.application.R;
import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.view.animation.AnimationUtils;
import android.widget.Filter;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.ViewFlipper;

public class LazyAdapter extends LazyAdapterBase {

	private Activity activity;
	private List<IWidgetItemInfo> data;
	private List<IWidgetItemInfo> originalData;
	private Filter filter = null;
	private static LayoutInflater inflater=null;


	public LazyAdapter(Activity a, List<IWidgetItemInfo> d) {
		activity = a;
		data=originalData=d;
		inflater = (LayoutInflater)activity.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		filter = new filter_here();
	}

	public int getCount() {
		return data.size();
	}

	public Object getItem(int position) {
		return position;
	}

	public long getItemId(int position) {
		return position;
	}

	public View getView(int position, View convertView, ViewGroup parent) {
		View vi=convertView;
		
		if(convertView==null)
			vi = inflater.inflate(R.layout.list_row, null);

		final Context mContext = vi.getContext();
		TextView title = (TextView)vi.findViewById(R.id.APPtitle); // title

		ImageView thumb_image=(ImageView)vi.findViewById(R.id.APP_list_image); // thumb image
		ImageButton leftBtn = (ImageButton)vi.findViewById(R.id.arrowLeft);
		ImageButton rightBtn = (ImageButton)vi.findViewById(R.id.arrowRight);
		final ViewFlipper vf = (ViewFlipper)vi.findViewById(R.id.viewFlipper1);
		final TextView statusOff = (TextView)vi.findViewById(R.id.AppStatus1); // duration
		final TextView statusAuto = (TextView)vi.findViewById(R.id.AppStatus2); // duration
		final TextView statusAlways = (TextView)vi.findViewById(R.id.AppStatus3); // duration
		
		final IWidgetItemInfo wi = data.get(position);
		
		leftBtn.setOnClickListener(new OnClickListener() {

			public void onClick(View v) {

				vf.setInAnimation(AnimationUtils.loadAnimation(mContext,
						R.anim.right_in));
				vf.setOutAnimation(AnimationUtils.loadAnimation(mContext,
						R.anim.right_out));
				vf.showPrevious();
				wi.setItemState(getSelectedState(vf, statusOff, statusAuto, statusAlways));
			}
		});

		rightBtn.setOnClickListener(new OnClickListener() {

			public void onClick(View v) {
				
				vf.setInAnimation(AnimationUtils.loadAnimation(mContext,
						R.anim.left_in));
				vf.setOutAnimation(AnimationUtils.loadAnimation(mContext,
						R.anim.left_out));
				vf.showNext();
				wi.setItemState(getSelectedState(vf, statusOff, statusAuto, statusAlways));
			}
		});
		
		// Setting all values in listview
		title.setText(wi.getLabel());
		
		switch (wi.getItemState()) {
		case AUTO:
			setFliperState(vf, statusAuto);
			break;
		case MUST:
			setFliperState(vf, statusAlways);
			break;
		case NOT_ALLOWED:
			setFliperState(vf, statusOff);
			break;

		default:
			break;
		}

		thumb_image.setImageBitmap(wi.getBitmap(mContext));
		return vi;
	}
	private void setFliperState(ViewFlipper vf, View v){
		while (!(vf.getCurrentView() == v)) {
			vf.showNext();
		}
	}
	private ItemState getSelectedState(ViewFlipper vf, View statusOff, View statusAuto, View statusAlways){
		View v = vf.getCurrentView();
		if (v == statusOff) {
			return ItemState.NOT_ALLOWED;
		}
		if (v == statusAuto) {
			return ItemState.AUTO;
		}
		if (v == statusAlways) {
			return ItemState.MUST;
		}
		
		return null;		
	}
	
	
    public Filter getFilter() {
        return filter ;
    }
	
	public class filter_here extends Filter{

        @Override
        protected FilterResults performFiltering(CharSequence constraint) {

            FilterResults Result = new FilterResults();
            if(constraint.length() == 0 ){
                Result.values = originalData;
                Result.count = originalData.size();
                return Result;
            }

            List<IWidgetItemInfo> filteredData = new ArrayList<IWidgetItemInfo>();
            String filterString = constraint.toString().toLowerCase();
            IWidgetItemInfo filterableString;

            for(int i = 0; i<originalData.size(); i++){
                filterableString = originalData.get(i);
                if(filterableString.getLabel().toLowerCase().contains(filterString)){
                    filteredData.add(filterableString);
                }
            }
            Result.values = filteredData;
            Result.count = filteredData.size();

            return Result;
        }

        @SuppressWarnings("unchecked")
		@Override
        protected void publishResults(CharSequence constraint,FilterResults results) {
        		data = ((List<IWidgetItemInfo>) results.values);	
            notifyDataSetChanged();
        }

    }
}
package coop.tecso.udaa.ui;

import java.util.ArrayList;
import java.util.TreeSet;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;
import coop.tecso.udaa.R;

public final class MenuSectionAdapter extends BaseAdapter {

    private static final int TYPE_CHECKBOX_VIEW = 0;
    private static final int TYPE_POPUP_VIEW = 1;
    private static final int TYPE_MAX_COUNT = TYPE_POPUP_VIEW + 1;

    private LayoutInflater mInflater;
    private ArrayList<MenuSectionItem> mData;
    private TreeSet<Integer> mCheckBoxesSet;

    public MenuSectionAdapter(Context context) {
    	mData = new ArrayList<>();
    	mCheckBoxesSet = new TreeSet<>();
        mInflater = (LayoutInflater) 
        	context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

    public void addTextView(final String label, final String description) {
        mData.add(new MenuSectionItem(TYPE_POPUP_VIEW, label, description));
        notifyDataSetChanged();
    }

    public void addCheckBoxView(final String label, final String description) {
    	mData.add(new MenuSectionItem(TYPE_CHECKBOX_VIEW, label, description));
        mCheckBoxesSet.add(mData.size() - 1);
        notifyDataSetChanged();
    }

    @Override
    public int getItemViewType(int position) {
        return mCheckBoxesSet.contains(position) ? TYPE_CHECKBOX_VIEW : TYPE_POPUP_VIEW;
    }

    @Override
    public int getViewTypeCount() {
        return TYPE_MAX_COUNT;
    }

    @Override
    public int getCount() {
        return mData.size();
    }

    @Override
    public MenuSectionItem getItem(int position) {
        return mData.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder holder = null;
        int type = getItemViewType(position);
        if (convertView == null) {
            holder = new ViewHolder();
            switch (type) {
                case TYPE_POPUP_VIEW:
                    convertView = mInflater.inflate(R.layout.list_item_popup, null);
                    convertView.setTag(holder);
                    holder.labelView = convertView.findViewById(R.id.list_item_popup_title);
                    holder.descriptionView = convertView.findViewById(R.id.list_item_popup_caption);
                    break;
                case TYPE_CHECKBOX_VIEW:
                    convertView = mInflater.inflate(R.layout.list_item_check, null);
                    convertView.setTag(holder);
                    holder.labelView = convertView.findViewById(R.id.list_item_check_title);
                    holder.descriptionView = convertView.findViewById(R.id.list_item_check_caption);
                    break;
            }
        } else {
            holder = (ViewHolder)convertView.getTag();
        }
        holder.labelView.setText(mData.get(position).label);
        holder.descriptionView.setText(mData.get(position).description);
        return convertView;
    }

	public static class ViewHolder {
	    TextView labelView;
	    TextView descriptionView;
	}
	
	public static class MenuSectionItem {
		String label;
		String description;
		
		MenuSectionItem(int type, String label, String description) {
			this.label = label;
			this.description = description;
		}
	}
}

package com.acarreos.creative.Adapters.Spinner;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.acarreos.creative.Models.TipoTransModel;
import com.acarreos.creative.R;

import java.util.ArrayList;

/**
 * Created by EnmanuelPc on 09/10/2015.
 */
public class SpinnerTipoTransAdapter extends BaseAdapter {

    private LayoutInflater mInflater;
    private Context context;
    private ArrayList<TipoTransModel> listItems;
    private boolean listChecked[];
    private CompoundButton.OnCheckedChangeListener mListener = new CompoundButton.OnCheckedChangeListener() {
        @Override
        public void onCheckedChanged(CompoundButton compoundButton, boolean isChecked) {
            listChecked[(int) compoundButton.getTag()] = isChecked;
            listenerItems.onItemSelected(getItemsSelected().size());
        }
    };

    public SpinnerTipoTransAdapter(Context context, ArrayList<TipoTransModel> listItems, OnItemSelectedListener listenerItems) {
        this.context = context;
        this.listItems = listItems;
        mInflater = LayoutInflater.from(context);
        listChecked = new boolean[listItems.size()];
        for (int i = 0; i < listItems.size(); i++) {
            listChecked[i] = false;
        }
        this.listenerItems = listenerItems;
    }

    @Override
    public int getCount() {
        return listItems.size();
    }

    @Override
    public Object getItem(int position) {
        return listItems.get(position);
    }

    @Override
    public long getItemId(int i) {
        return 0;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        return getCustomView(position, convertView, parent);
    }

    @Override
    public View getDropDownView(int position, View convertView, ViewGroup parent) {
        return getCustomView(position, convertView, parent);
    }

    public View getCustomView(int position, View convertView, ViewGroup parent) {
        ViewHolder holder;
        if (convertView == null) {
            convertView = mInflater.inflate(R.layout.spinner_radio_item, parent, false);
            holder = new ViewHolder(convertView);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }
        final TipoTransModel transInfo = listItems.get(position);
        holder.txtDesc.setText(transInfo.getNombre());
        holder.btnVerDetalle.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //TODO mostrar detalle de transporte
                Toast.makeText(context, "Mostrando: " + transInfo.getDescripcion(), Toast.LENGTH_SHORT).show();
            }
        });
        holder.checkSelect.setTag(position);
        holder.checkSelect.setChecked(listChecked[position]);
        holder.checkSelect.setOnCheckedChangeListener(mListener);
        return convertView;
    }

    public ArrayList<TipoTransModel> getItemsSelected() {
        ArrayList<TipoTransModel> listSelected = new ArrayList<>();
        for (int i = 0; i < getCount(); i++) {
            if (listChecked[i]) {
                listSelected.add(listItems.get(i));
            }
        }
        return listSelected;
    }


    private static class ViewHolder {
        TextView txtDesc;
        ImageView btnVerDetalle;
        CheckBox checkSelect;

        public ViewHolder(View itemView) {
            txtDesc = (TextView) itemView.findViewById(R.id.txtDescTrans);
            btnVerDetalle = (ImageView) itemView.findViewById(R.id.btnDetalleTrans);
            checkSelect = (CheckBox) itemView.findViewById(R.id.checkSpinner);
        }
    }

    private OnItemSelectedListener listenerItems;

    public interface OnItemSelectedListener {
        void onItemSelected(int numItemsSelected);
    }

}

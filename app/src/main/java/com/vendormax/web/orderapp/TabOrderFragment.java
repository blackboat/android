package com.vendormax.web.orderapp;


import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.ListView;
import android.widget.NumberPicker;
import android.widget.TextView;
import android.widget.Toast;

import com.loopj.android.http.JsonHttpResponseHandler;
import com.vendormax.web.orderapp.api.OrderAPI;

import org.apache.http.Header;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


/**
 * A simple {@link Fragment} subclass.
 */
public class TabOrderFragment extends Fragment {

    private ListView listView;
    private LayoutInflater mInflater;
    private MyAdapter myAdapter;
    private EditText etPurchase, etSearch;
    private Button btnConfirm;
    private JSONArray json_orders;
    private String delivery_date;
    private String send_message;

    public TabOrderFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        final View v = inflater.inflate(R.layout.fragment_tab_order, container, false);
        etPurchase = (EditText) v.findViewById(R.id.tab_order_edit_purchase);
        etSearch = (EditText) v.findViewById(R.id.tab_order_edit_search);
        mInflater = inflater;
        delivery_date = "";

        HashMap<String, String> params = new HashMap<String, String>();

        params.put("auth_token", OrderAPI.token);
        params.put("user_id", OrderAPI.user_id);
        params.put("account_id", OrderAPI.customer_id);
        final Context context = this.getActivity();

        final ProgressDialog progressDialog = ProgressDialog.show(context, "Loading Orders", "Please wait...", true);

        OrderAPI.postOrders(params, new JsonHttpResponseHandler() {
            public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                if (progressDialog.isShowing()) {
                    progressDialog.dismiss();
                }
                Log.d("Success", response.toString());
                try {
                    String isSuccess = response.getString("success");
                    if (!isSuccess.equals("true")) {
                        Toast.makeText(context, "Fail. Server is not working.", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    json_orders = response.getJSONArray("orders");
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                listView = (ListView) v.findViewById(R.id.listView);
                btnConfirm = (Button) v.findViewById(R.id.tab_order_button_confirm_order);
                btnConfirm.setEnabled(true);
                listView.setItemsCanFocus(true);
                myAdapter = new MyAdapter();
                listView.setAdapter(myAdapter);

                listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                    @Override
                    public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                        listView.setEnabled(false);
                        final TextView v = (TextView) view.findViewById(R.id.order_item_quantity);
                        AlertDialog alert = new AlertDialog.Builder(context).create();
                        alert.setTitle("Quantity");
                        alert.setMessage("Please set quantity of product.");
                        final NumberPicker input = new NumberPicker(context);
                        input.setMinValue(1);
                        input.setMaxValue(100);
                        ListItem listItem = (ListItem) myAdapter.filteredData.get(i);
                        if (!listItem.isEmptyQuantity())
                            input.setValue(Integer.parseInt(listItem.quantity));
                        alert.setView(input);
                        final int index = i;
                        alert.setButton(DialogInterface.BUTTON_POSITIVE, "OK", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                String quantity = String.valueOf(input.getValue());
                                v.setText(quantity);
                                ListItem listItem = (ListItem) myAdapter.filteredData.get(index);
                                listItem.quantity = quantity;
                                dialogInterface.dismiss();
                                int start = listView.getFirstVisiblePosition();
                                listView.getAdapter().getView(index, listView.getChildAt(index-start), listView);
                            }
                        });
                        alert.setButton(DialogInterface.BUTTON_NEGATIVE, "Cancel", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                dialogInterface.dismiss();
                            }
                        });
                        alert.setButton(DialogInterface.BUTTON_NEUTRAL, "Clear", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                v.setText("");
                                ListItem listItem = (ListItem) myAdapter.filteredData.get(index);
                                listItem.quantity = "";
                                dialogInterface.dismiss();
                                int start = listView.getFirstVisiblePosition();
                                listView.getAdapter().getView(index, listView.getChildAt(index - start), listView);
                            }
                        });
                        alert.setOnDismissListener(new DialogInterface.OnDismissListener() {
                            @Override
                            public void onDismiss(DialogInterface dialogInterface) {
                                listView.setEnabled(true);
                            }
                        });
                        alert.show();
                    }
                });

                btnConfirm.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        if (isListQuantitiesEmpty()) {
                            Toast.makeText(context, "Set quantity of order you want", Toast.LENGTH_SHORT).show();
                            return;
                        }

                        AlertDialog alert = new AlertDialog.Builder(context).create();
                        alert.setTitle("Confirm Delivery Day");
                        alert.setMessage("Is this order for tomorrow?");
                        alert.setButton(DialogInterface.BUTTON_POSITIVE, "Yes", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                Calendar calendar = Calendar.getInstance();
                                calendar.add(Calendar.DATE, 1);
                                SimpleDateFormat sdf = new SimpleDateFormat("yyyy/MM/dd");
                                delivery_date = sdf.format(calendar.getTime());
                                Log.d("DAte", delivery_date);
                                dialogInterface.dismiss();
                                createSendMsgAlert();
                            }
                        });
                        alert.setButton(DialogInterface.BUTTON_NEGATIVE, "No", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                Calendar calendar = Calendar.getInstance();
                                int year = calendar.get(Calendar.YEAR), month = calendar.get(Calendar.MONTH), day = calendar.get(Calendar.DAY_OF_MONTH);
                                dialogInterface.dismiss();

                                final DatePickerDialog.OnDateSetListener datePickerDialogCallback = new DatePickerDialog.OnDateSetListener() {
                                    @Override
                                    public void onDateSet(DatePicker datePicker, int i, int i2, int i3) {
                                        int month = i2 + 1;
                                        delivery_date = i + "/" + month + "/" + i3;
                                    }
                                };

                                final DatePickerDialog datePickerDialog = new DatePickerDialog(context, datePickerDialogCallback, year, month, day+2);
                                datePickerDialog.setButton(DialogInterface.BUTTON_POSITIVE, "Set", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dlgInterface, int i) {
                                        dlgInterface.dismiss();
                                        DatePicker datePicker = datePickerDialog.getDatePicker();
                                        datePickerDialogCallback.onDateSet(datePicker, datePicker.getYear(), datePicker.getMonth(), datePicker.getDayOfMonth());
                                        createSendMsgAlert();
                                    }
                                });
                                datePickerDialog.setButton(DialogInterface.BUTTON_NEGATIVE, "Cancel", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dlgInterface, int i) {
                                        delivery_date = "";
                                        dlgInterface.dismiss();
                                    }
                                });
                                datePickerDialog.show();
                            }
                        });
                        alert.setButton(DialogInterface.BUTTON_NEUTRAL, "Cancel", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                dialogInterface.dismiss();
                            }
                        });
                        alert.show();
                    }
                });

                etSearch.addTextChangedListener(new TextWatcher() {
                    @Override
                    public void beforeTextChanged(CharSequence charSequence, int i, int i2, int i3) {

                    }

                    @Override
                    public void onTextChanged(CharSequence charSequence, int i, int i2, int i3) {
                        myAdapter.getFilter().filter(charSequence.toString());
                    }

                    @Override
                    public void afterTextChanged(Editable editable) {

                    }
                });
            }

            public void onFailure(int statusCode, Header[] headers, Throwable throwable, JSONObject response) {
                Log.d("Fail", "dsf");
                progressDialog.dismiss();
            }

            public void onFailure(int statusCode, Header[] headers, String response, Throwable throwable) {
                Log.d("Fail", response);
                progressDialog.dismiss();
            }
        });


        return v;
    }

    public boolean isListQuantitiesEmpty() {
        int count = myAdapter.originalData.size();
        for (int i = 0; i < count; i++) {
            ListItem listItem = (ListItem) myAdapter.originalData.get(i);
            if (!listItem.isEmptyQuantity()) {
                return false;
            }
        }
        return true;
    }

    public void createSendMsgAlert() {
        Context context = this.getActivity();
        final AlertDialog alert = new AlertDialog.Builder(context).create();
        alert.setTitle("Order Message");
        alert.setMessage("Send me a message");
        final EditText input = new EditText(context);
        input.setHint("(optional)");
        alert.setView(input);
        alert.setButton(DialogInterface.BUTTON_POSITIVE, "Send Order!", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                send_message = input.getText().toString();
                dialogInterface.dismiss();
                postPurchase(etPurchase.getText().toString(), send_message, delivery_date);
            }
        });
        alert.setButton(DialogInterface.BUTTON_NEGATIVE, "Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                dialogInterface.dismiss();
                Toast.makeText(getActivity(), "Cancel sending orders", Toast.LENGTH_SHORT).show();
            }
        });
        alert.show();
    }

    public void postPurchase(String purchase, String message, String date) {
        Log.d("purchase", purchase);
        Log.d("message", message);
        Log.d("date", date);
        HashMap<String, Object> params = new HashMap<String, Object>();
        params.put("auth_token", OrderAPI.token);
        params.put("user_id", OrderAPI.user_id);
        params.put("account_id", OrderAPI.customer_id);
        params.put("purchase_order", purchase);
        params.put("message", message);
        params.put("delivery_date", delivery_date);
        List<Map<String, String>> listOfMaps = new ArrayList<Map<String, String>>();
        int count = myAdapter.originalData.size();

        for (int i = 0; i < count; i++) {
            ListItem listItem = (ListItem) myAdapter.originalData.get(i);
            if (!listItem.isEmptyQuantity()) {
                Map<String, String> item = new HashMap<String, String>();
                item.put("code", listItem.code);
                item.put("supplier", listItem.supplier);
                item.put("quantity", listItem.quantity);
                listOfMaps.add(item);
            }
        }
        params.put("list", listOfMaps);
        Log.d("params", params.toString());

        final Context context = this.getActivity();
        final ProgressDialog progressDialog = ProgressDialog.show(context, "Sending Orders", "Please wait...", true);

        OrderAPI.postPurchase(params, new JsonHttpResponseHandler() {
            public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                progressDialog.dismiss();
                Log.d("Success", response.toString());
                try {
                    String isSuccess = response.getString("success");
                    if (!isSuccess.equals("true")) {
                        Toast.makeText(context, "Sorry. Sending orders is failed.", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    Toast.makeText(context, "Success", Toast.LENGTH_LONG).show();
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
            public void onFailure(int statusCode, Header[] headers, Throwable throwable, JSONObject response) {
                Toast.makeText(context, "Sorry. Sending orders is failed.", Toast.LENGTH_SHORT).show();
                progressDialog.dismiss();
                Log.d("Fail", "dsf");
            }
            public void onFailure(int statusCode, Header[] headers, String response, Throwable throwable) {
                progressDialog.dismiss();
                Log.d("Fail", response);
            }
        });
    }

    public class MyAdapter extends BaseAdapter implements Filterable {
        public ArrayList originalData = new ArrayList();
        public ArrayList filteredData = new ArrayList();
        private ItemFilter mFilter = new ItemFilter();

        public MyAdapter() {
            int count = json_orders.length();

            for (int i = 0; i < count; i++) {
                JSONObject json_item;
                String code, supplier, description;
                try {
                    json_item = json_orders.getJSONObject(i);
                    code = json_item.getString("code");
                    supplier = json_item.getString("supplier");
                    description = json_item.getString("product_description");
                } catch (JSONException e) {
                    e.printStackTrace();
                    return;
                }

                ListItem listItem = new ListItem();
                listItem.code = code;
                listItem.supplier = supplier;
                listItem.description = description;
                listItem.quantity = "";
                originalData.add(listItem);
                filteredData.add(listItem);
            }
            notifyDataSetChanged();
        }

        public int getCount() {
            return filteredData.size();
        }

        public Object getItem(int position) {
            return position;
        }

        public long getItemId(int position) {
            return position;
        }

        public View getView(int position, View convertView, ViewGroup parent) {
            ViewHolder holder;
            if (convertView == null) {
                holder = new ViewHolder();
                convertView = mInflater.inflate(R.layout.list_order_item, null);
                holder.supplier = (TextView) convertView.findViewById(R.id.order_item_supplier);
                holder.description = (TextView) convertView.findViewById(R.id.order_item_description);
                holder.quantity = (TextView) convertView.findViewById(R.id.order_item_quantity);
                convertView.setTag(holder);
            } else {
                holder = (ViewHolder) convertView.getTag();
            }

            ListItem listItem = (ListItem)filteredData.get(position);
            holder.supplier.setText(listItem.supplier);
            holder.description.setText(listItem.description);
            holder.quantity.setText(listItem.quantity);
            if (listItem.isEmptyQuantity()) {
                convertView.setBackgroundResource(R.color.transparent);
            } else {
                convertView.setBackgroundResource(R.color.light_blue);
            }

            return convertView;
        }

        @Override
        public Filter getFilter() {
            return mFilter;
        }

        private class ItemFilter extends Filter {

            @Override
            protected FilterResults performFiltering(CharSequence charSequence) {
                String filterString = charSequence.toString().toLowerCase();
                FilterResults results = new FilterResults();
                ArrayList list = originalData;
                int count = list.size();
                ArrayList nlist = new ArrayList(count);
                String filterableString;

                for (int i = 0; i < count; i++) {
                    filterableString = ((ListItem)list.get(i)).description;
                    if (filterableString.toLowerCase().contains(filterString))
                        nlist.add(list.get(i));
                }

                results.values = nlist;
                results.count = nlist.size();
                return results;
            }

            @Override
            protected void publishResults(CharSequence charSequence, FilterResults filterResults) {
                filteredData = (ArrayList) filterResults.values;
                notifyDataSetChanged();
            }
        }
    }

    class ViewHolder {
        TextView supplier;
        TextView description;
        TextView quantity;
    }

    class ListItem {
        String code;
        String supplier;
        String description;
        String quantity="";

        public boolean isEmptyQuantity() {
            return quantity.isEmpty();
        }
    }
}

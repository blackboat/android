package com.vendormax.web.orderapp;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
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
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.loopj.android.http.JsonHttpResponseHandler;
import com.loopj.android.http.RequestParams;
import com.vendormax.web.orderapp.api.OrderAPI;

import org.apache.http.Header;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;


/**
 * A simple {@link Fragment} subclass.
 */
public class TabProductFragment extends Fragment {

    private ListView listView;
    private LayoutInflater mInflater;
    private MyAdapter myAdapter;
    private EditText etSearch;
    private Button btnAdd;
    private JSONArray json_products;

    public TabProductFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        final View v = inflater.inflate(R.layout.fragment_tab_product, container, false);
        final Context context = this.getActivity();

        etSearch = (EditText) v.findViewById(R.id.tab_product_edit_search);

        HashMap<String, String> params = new HashMap<String, String>();

        params.put("auth_token", OrderAPI.token);
        params.put("user_id", OrderAPI.user_id);
        params.put("account_id", OrderAPI.customer_id);

        final LayoutInflater finInflater = inflater;

        final ProgressDialog progressDialog = ProgressDialog.show(this.getActivity(), "Loading Products", "Please wait...", true);

        OrderAPI.postProducts(params, new JsonHttpResponseHandler() {
            public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                progressDialog.dismiss();
                Log.d("Success", response.toString());
                try {
                    String isSuccess = response.getString("success");
                    if (!isSuccess.equals("true")) {
                        Toast.makeText(context, "Fail. Server is not working.", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    json_products = response.getJSONArray("products");
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                mInflater = finInflater;
                listView = (ListView) v.findViewById(R.id.listView);
                listView.setItemsCanFocus(true);
                myAdapter = new MyAdapter();
                listView.setAdapter(myAdapter);

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

                btnAdd = (Button) v.findViewById(R.id.tab_product_button_add_order);
                btnAdd.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        HashMap<String, Object> params = new HashMap<String, Object>();
                        params.put("auth_token", OrderAPI.token);
                        params.put("user_id", OrderAPI.user_id);
                        params.put("account_id", OrderAPI.customer_id);
                        List<Map<String, String>> listOfMaps = new ArrayList<Map<String, String>>();
                        final int count = myAdapter.originalData.size();
                        int checked_count = 0;
                        for (int i = 0; i < count; i++) {
                            ListItem listItem = (ListItem) myAdapter.originalData.get(i);
                            boolean bCheck = listItem.check;
                            if (bCheck) {
                                checked_count += 1;
                                Map<String, String> item = new HashMap<String, String>();
                                item.put("code", listItem.code);
                                item.put("supplier", listItem.supplier);
                                item.put("product_description", listItem.description);
                                item.put("product_group", listItem.group);
                                listOfMaps.add(item);
                            }
                        }
                        final int order_count = checked_count;
                        params.put("orders", listOfMaps);
                        Log.d("abc", params.toString());
                        final ProgressDialog progressDialog = ProgressDialog.show(context, "Sending Products", "Please wait...", true);
                        OrderAPI.postCheckedProduct(params, new JsonHttpResponseHandler() {
                            public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                                progressDialog.dismiss();
                                Log.d("Success", response.toString());
                                try {
                                    String isSuccess = response.getString("success");
                                    if (!isSuccess.equals("true")) {
                                        Toast.makeText(context, "Sorry. Sending products to order page is failed.", Toast.LENGTH_SHORT).show();
                                        return;
                                    }
                                    Toast.makeText(context, order_count + "products have been added to orders page.", Toast.LENGTH_SHORT).show();
                                } catch (JSONException e) {
                                    e.printStackTrace();
                                }
                            }
                            public void onFailure(int statusCode, Header[] headers, Throwable throwable, JSONObject response) {
                                Toast.makeText(context, "Sorry. Sending products to order page is failed.", Toast.LENGTH_SHORT).show();
                                progressDialog.dismiss();
                                Log.d("Fail", "dsf");
                            }
                            public void onFailure(int statusCode, Header[] headers, String response, Throwable throwable) {
                                Toast.makeText(context, "Sorry. Sending products to order page is failed.", Toast.LENGTH_SHORT).show();
                                progressDialog.dismiss();
                                Log.d("Fail", response);
                            }
                        });
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

    public class MyAdapter extends BaseAdapter implements Filterable {
//        private LayoutInflater mInflater;
        public ArrayList originalData = new ArrayList();
        public ArrayList filteredData = new ArrayList();
        private ItemFilter mFilter = new ItemFilter();

        public MyAdapter() {
            int count = json_products.length();

            for (int i = 0; i < count; i++) {
                JSONObject json_item;
                String code, supplier, description, group;
                try {
                    json_item = json_products.getJSONObject(i);
                    code = json_item.getString("prod_code");
                    supplier = json_item.getString("supplier");
                    description = json_item.getString("prod_description");
                    group = json_item.getString("prod_grp");
                } catch (JSONException e) {
                    e.printStackTrace();
                    return;
                }

                ListItem listItem = new ListItem();
                listItem.code = code;
                listItem.supplier = supplier;
                listItem.description = description;
                listItem.group = group;
                //listItem.check = false;

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
            final ViewHolder holder;
            if (convertView == null) {
                holder = new ViewHolder();
                convertView = mInflater.inflate(R.layout.list_product_item, null);
                holder.supplier = (TextView) convertView.findViewById(R.id.product_item_supplier);
                holder.description = (TextView) convertView.findViewById(R.id.product_item_description);
                holder.check = (CheckBox) convertView.findViewById(R.id.product_item_check);
                convertView.setTag(holder);
            } else {
                holder = (ViewHolder) convertView.getTag();
            }

            final ListItem listItem = (ListItem) filteredData.get(position);
            holder.supplier.setText(listItem.supplier);
            holder.description.setText(listItem.description);
            holder.check.setChecked(listItem.check);
            holder.check.setOnClickListener(new View.OnClickListener() {

                @Override
                public void onClick(View view) {
                    listItem.check = holder.check.isChecked();
                }
            });

            return convertView;
        }

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
        CheckBox check;
    }

    class ListItem {
        String code;
        String supplier;
        String description;
        String group;
        Boolean check=false;
    }
}

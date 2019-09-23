package com.app.markeet;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.opengl.Visibility;
import android.os.Bundle;
import com.google.android.material.snackbar.Snackbar;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.appcompat.widget.Toolbar;
import android.os.PersistableBundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.TextView;
import com.app.markeet.adapter.AdapterShoppingCart;
import com.app.markeet.data.DatabaseHandler;
import com.app.markeet.data.SharedPref;
import com.app.markeet.model.Cart;
import com.app.markeet.model.Info;
import com.app.markeet.utils.Tools;
import java.util.List;
import static com.app.markeet.R.id.lyt_no_item;

public class ActivityShoppingCart extends AppCompatActivity {

    private View parent_view;
    private RecyclerView recyclerView;
    private DatabaseHandler db;
    private AdapterShoppingCart adapter;
    private TextView price_total;
    private SharedPref sharedPref;
    private Info info;
    private RadioButton rBmyself;
    private RadioButton rBdilivery;
    final static String textView = "TEXTVIEW";
    public RadioButton getrBmyself() {
        return rBmyself;
    }
    public RadioButton getrBdilivery() {
        return rBdilivery;
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_shopping_cart);
        db = new DatabaseHandler(this);
        sharedPref = new SharedPref(this);
        info = sharedPref.getInfoData();

        initToolbar();
        iniComponent();
    }

    private void iniComponent() {
        parent_view = findViewById(android.R.id.content);
        recyclerView = (RecyclerView) findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        price_total = (TextView) findViewById(R.id.price_total);
        rBmyself = (RadioButton) findViewById(R.id.rbutton_with_myself);
        rBdilivery = (RadioButton) findViewById(R.id.rbutton_dilivery);
    }

    private void initToolbar() {
        ActionBar actionBar;
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        actionBar = getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setHomeButtonEnabled(true);
        actionBar.setTitle(R.string.title_activity_cart);
        //  Tools.systemBarLolipop(this);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {//стрелка "назад" переход на предыдущую активность. Одинаково, что нажать кнопку для выхода
        int item_id = item.getItemId();
        if (item_id == android.R.id.home) {
            super.onBackPressed();
        }
        return super.onOptionsItemSelected(item);
    }

//    @Override
//    public boolean onCreateOptionsMenu(Menu menu) {
//        getMenuInflater().inflate(R.menu.menu_activity_shopping_cart, menu);
//        return true;//if true then меню отображается на тулбаре
    //   }

//    @Override
//    public boolean onOptionsItemSelected(MenuItem item) {
//        int item_id = item.getItemId();
//        if (item_id == android.R.id.home) {
//            super.onBackPressed();
//        } else if (item_id == R.id.action_checkout) {
//            if (adapter.getItemCount() > 0) {
//                Intent intent = new Intent(ActivityShoppingCart.this, ActivityCheckout.class);
//                startActivity(intent);
//            } else {
//                Snackbar.make(parent_view, R.string.msg_cart_empty, Snackbar.LENGTH_SHORT).show();
//            }
//        } else if (item_id == R.id.action_delete) {
//            if (adapter.getItemCount() == 0) {
//                Snackbar.make(parent_view, R.string.msg_cart_empty, Snackbar.LENGTH_SHORT).show();
//                return true;
//            }
//            dialogDeleteConfirmation();
    //       }
    //       return super.onOptionsItemSelected(item);
    //      }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
          String textViewText = savedInstanceState.getString(textView);
          price_total.setText(textViewText);
    }

    @Override
    protected void onResume() {
        super.onResume();
        displayData();
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        outState.putString(textView, price_total.getText().toString());
        super.onSaveInstanceState(outState);
    }

    private void displayData() {
        List<Cart> items = db.getActiveCartList();
        adapter = new AdapterShoppingCart(this, true, items);
        recyclerView.setAdapter(adapter);
        recyclerView.setNestedScrollingEnabled(false);

        adapter.setOnItemClickListener(new AdapterShoppingCart.OnItemClickListener() {
            @Override
            public void onItemClick(View view, Cart obj) {
                dialogCartAction(obj);
            }
        });
        LinearLayout lyt_add_services = (LinearLayout) findViewById(R.id.lyt_add_services);//Добавлен Linearlayout для допуслуг
        View lyt_no_item = (View) findViewById(R.id.lyt_no_item);
        if (adapter.getItemCount() == 0) {
            lyt_no_item.setVisibility(View.VISIBLE);
            lyt_add_services.setVisibility(View.INVISIBLE);//здесь он пропадает, когда товара в корзине нет
        } else {
            lyt_no_item.setVisibility(View.GONE);
        }
        setTotalPrice();
    }

    public void setTotalPrice() {
        List<Cart> items = adapter.getItem();
        Double _price_total = 0D;
        Double total_self = 0D;
        Double total_delivery = 0D;
        final Double _price_add_self = 15D;
        final Double _price_add_delivery = 60D;
       // String _price_total_tax_str;// чистая цена без доставки
        final String self;
        final String delivery;

        for (Cart c : items) {
            _price_total = _price_total + (c.amount * c.price_item);
        }
        total_self = _price_total + _price_add_self;
        total_delivery = _price_total + _price_add_delivery;

        delivery = Tools.getFormattedPrice(total_delivery, this);
        self = Tools.getFormattedPrice(total_self, this);
       // _price_total_tax_str = Tools.getFormattedPrice(_price_total, this);// чистая цена без доставки

        rBmyself.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (rBmyself.isChecked()) {
                   price_total.setText(self);
                }
            }
        });
        rBdilivery.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (rBdilivery.isChecked()) {
                    price_total.setText(delivery);
                }
            }
        });
           // price_total.setText(_price_total_tax_str);// чистая цена без доставки
                }

       private void dialogCartAction (final Cart model){

            final Dialog dialog = new Dialog(ActivityShoppingCart.this);
            dialog.requestWindowFeature(Window.FEATURE_NO_TITLE); // before
            dialog.setContentView(R.layout.dialog_cart_option);

            WindowManager.LayoutParams lp = new WindowManager.LayoutParams();
            lp.copyFrom(dialog.getWindow().getAttributes());
            lp.width = WindowManager.LayoutParams.MATCH_PARENT;
            lp.height = WindowManager.LayoutParams.WRAP_CONTENT;
            ((TextView) dialog.findViewById(R.id.title)).setText(model.product_name);
            ((TextView) dialog.findViewById(R.id.stock)).setText(getString(R.string.stock) + model.stock);
            final TextView qty = (TextView) dialog.findViewById(R.id.quantity);
            qty.setText(model.amount + "");

            ((ImageView) dialog.findViewById(R.id.img_decrease)).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if (model.amount > 1) {
                        model.amount = model.amount - 1;
                        qty.setText(model.amount + "");
                    }
                }
            });
            ((ImageView) dialog.findViewById(R.id.img_increase)).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if (model.amount < model.stock) {
                        model.amount = model.amount + 1;
                        qty.setText(model.amount + "");
                    }
                }
            });
            ((Button) dialog.findViewById(R.id.bt_save)).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    db.saveCart(model);
                    displayData();
                    dialog.dismiss();
                }
            });
            ((Button) dialog.findViewById(R.id.bt_remove)).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    db.deleteActiveCart(model.product_id);
                    displayData();
                    dialog.dismiss();
                    price_total.setText("");
                }
            });
            dialog.show();
            dialog.getWindow().setAttributes(lp);
        }

        public void dialogDeleteConfirmation () {
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle(R.string.title_delete_confirm);
            builder.setMessage(getString(R.string.content_delete_confirm));
            builder.setPositiveButton(R.string.YES, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface di, int i) {
                    di.dismiss();
                    db.deleteActiveCart();
                    onResume();
                    Snackbar.make(parent_view, R.string.delete_success, Snackbar.LENGTH_SHORT).show();
                    price_total.setText("");
                }
            });
            builder.setNegativeButton(R.string.CANCEL, null);
            builder.show();
        }

     public void onClicked (View view){
            switch (view.getId()) {

                case R.id.action_checkout:
                    if (!rBmyself.isChecked() && !rBdilivery.isChecked() && adapter.getItemCount() > 0){
                        Snackbar.make(parent_view, R.string.choose_shipping_cart, Snackbar.LENGTH_SHORT).show();
                    }
                    else if (adapter.getItemCount() > 0) {
                            Intent intent = new Intent(ActivityShoppingCart.this, ActivityCheckout.class);
                            //отсюда
                            boolean self_bool = getrBmyself().isChecked();
                            boolean delivery_bool = getrBdilivery().isChecked();
                            intent.putExtra("FLAG_SELF", self_bool);
                            intent.putExtra("FLAG_DELIVERY", delivery_bool);
                            //до сюда код переноса значений флагов в следующую активность
                            startActivity(intent);
                        } else {
                        Snackbar.make(parent_view, R.string.msg_cart_empty, Snackbar.LENGTH_SHORT).show();
                        }
                    break;

                case R.id.action_delete:
                    if (adapter.getItemCount() == 0) {
                        Snackbar.make(parent_view, R.string.msg_cart_empty, Snackbar.LENGTH_SHORT).show();
                    } else
                        dialogDeleteConfirmation();
                        break;
                }
            }
        }

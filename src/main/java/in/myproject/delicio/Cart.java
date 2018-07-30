package in.myproject.delicio;

import android.content.DialogInterface;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import in.myproject.delicio.Common.Common;
import in.myproject.delicio.Database.Database;
import in.myproject.delicio.Model.Order;
import in.myproject.delicio.Model.Request;
import in.myproject.delicio.ViewHolder.CartAdapter;
import info.hoang8f.widget.FButton;

public class Cart extends AppCompatActivity {

    RecyclerView recyclerView;
    RecyclerView.LayoutManager layoutManager;

    FirebaseDatabase database;
    DatabaseReference requests;

    TextView txtTotalPrice;
    FButton btnPlace;

    List<Order> cart=new ArrayList<>();
    CartAdapter adapter;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cart);

        //firebase
        database=FirebaseDatabase.getInstance();
        requests=database.getReference("Requests");

        //Init
        recyclerView=(RecyclerView) findViewById(R.id.listCart);
        recyclerView.setHasFixedSize(true);
        layoutManager=new LinearLayoutManager(this);
        recyclerView.setLayoutManager(layoutManager);


        txtTotalPrice=(TextView) findViewById(R.id.total);
        btnPlace=(FButton)findViewById(R.id.btnPlaceOrder);

        btnPlace.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // creating new request

                showAlertDialog();
            }
        });

            loadListFood();

    }

    private void showAlertDialog() {

        AlertDialog.Builder a = new AlertDialog.Builder(Cart.this);
        a.setTitle("One More Step!");
        a.setMessage("Enter your Address: ");

        final EditText edtAddress =new EditText(Cart.this);
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.MATCH_PARENT
        );
        edtAddress.setLayoutParams(lp);
        a.setView(edtAddress);//add edit text to alert dialog
        a.setIcon(R.drawable.ic_shopping_cart_black_24dp);

        a.setPositiveButton("YES", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                //Create new Request
                Request request = new Request(
                    Common.currentUser.getPhone(),
                    Common.currentUser.getName(),
                    edtAddress.getText().toString(),
                    txtTotalPrice.getText().toString(),
                    cart

                );

                //submit to firebase
                //using System.CurrentMilli to key
                requests.child(String.valueOf(System.currentTimeMillis()))
                        .setValue(request);

                // delete from cart
                new Database(getBaseContext()).cleanCart();
                Toast.makeText(Cart.this, "Thank you, Order Place", Toast.LENGTH_SHORT).show();

                finish();

            }
        });

        a.setNegativeButton("NO", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });

        a.show();
    }

    private  void loadListFood()
    {
        cart=new Database( this).getCarts();
        adapter=new CartAdapter(cart,this);
        recyclerView.setAdapter(adapter);

        //calculate total price
        int total=0;
        for (Order order:cart)
        {
            total+=(Integer.parseInt(order.getPrice()))*(Integer.parseInt(order.getQuantity()));
            Locale locale=new Locale("en","IN");
            NumberFormat fmt=NumberFormat.getCurrencyInstance(locale);
            txtTotalPrice.setText(fmt.format(total));
        }

    }
}

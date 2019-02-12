package tony.dev.mohamed.myuberridder;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.AppCompatButton;
import android.support.v7.widget.AppCompatEditText;
import android.view.View;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.DecimalFormat;
import java.util.HashMap;
import java.util.Map;

import dmax.dialog.SpotsDialog;
import me.zhanghai.android.materialratingbar.MaterialRatingBar;
import tony.dev.mohamed.myuberridder.helper.Common;
import tony.dev.mohamed.myuberridder.models.Rate;

public class RatingActivity extends AppCompatActivity {
    double ratingNum;
    private FirebaseDatabase mDatabase;
    private AppCompatButton sendButton;
    private AppCompatEditText commentEdit;
    private DatabaseReference mDriverReference;
    private DatabaseReference mRateReference;
    private MaterialRatingBar ratingBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_rating);

        //init firebase
        mDatabase = FirebaseDatabase.getInstance();
        mDriverReference = mDatabase.getReference(Common.user_driver_tbl);
        mRateReference = mDatabase.getReference(Common.rate_detial_tbl);

        //init views
        ratingBar = findViewById(R.id.ratingBar);
        commentEdit = findViewById(R.id.comment);
        sendButton = findViewById(R.id.sendButton);

        ratingBar.setOnRatingChangeListener(new MaterialRatingBar.OnRatingChangeListener() {
            @Override
            public void onRatingChanged(MaterialRatingBar ratingBar, float rating) {
                ratingNum = rating;
            }
        });
        sendButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                submit(Common.mDriverId);
            }
        });


    }

    private void submit(final String mDriverId) {
        final SpotsDialog dialog = new SpotsDialog(this);
        dialog.show();

        //
        Rate rate = new Rate(String.valueOf(ratingNum), commentEdit.getText().toString());
        // send values to the server
        mRateReference.child(mDriverId).push().setValue(rate).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if (task.isSuccessful()) {
                    mRateReference.child(mDriverId).addValueEventListener(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                            double avergeStars = 0.0;
                            int count = 0;
                            for (DataSnapshot dataSnapshot1 : dataSnapshot.getChildren()) {
                                Rate rate1 = dataSnapshot1.getValue(Rate.class);
                                if (rate1 != null) {
                                    avergeStars += Double.parseDouble(rate1.getRates());
                                    count++;
                                }

                            }
                            double finalAvg = avergeStars / count;
                            DecimalFormat decimalFormat = new DecimalFormat("#.#");
                            String value = decimalFormat.format(finalAvg);

                            Map<String, Object> map = new HashMap<>();
                            map.put("rates", value);
                            mDriverReference.child(mDriverId).updateChildren(map).addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task) {
                                    if (task.isSuccessful()) {
                                        dialog.dismiss();
                                        Toast.makeText(RatingActivity.this, "Thank You For your submit !", Toast.LENGTH_SHORT).show();
                                        finish();
                                    }

                                }
                            }).addOnFailureListener(new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull Exception e) {
                                    dialog.dismiss();
                                    Toast.makeText(RatingActivity.this, "Fail !", Toast.LENGTH_SHORT).show();
                                }
                            });

                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError databaseError) {

                        }
                    });
                } else {
                    if (task.getException() != null) {
                        Toast.makeText(RatingActivity.this, " Error" + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                    }
                }
            }
        });
    }
}

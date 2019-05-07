package android.com.identificatecadministrador;


import android.content.Intent;
import android.os.Bundle;
import android.security.ConfirmationNotAvailableException;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import static android.app.Activity.RESULT_OK;

public class LostCredentials extends Fragment {


    public LostCredentials() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_lost_credentials, container, false);
    }

    EditText matricula;
    Button searchMatricula;
    TextView isLost;
    TextView foundBy;
    Button changeIsLost;

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        matricula = view.findViewById(R.id.matricula_lost_edit_text);
        searchMatricula = view.findViewById(R.id.search_lost_button);
        isLost = view.findViewById(R.id.islost_text_view);
        foundBy = view.findViewById(R.id.foundby_lost_text_view);
        changeIsLost = view.findViewById(R.id.change_lost_button);
        changeIsLost.setEnabled(false);

        final DatabaseReference db = FirebaseDatabase.getInstance().getReference();

        searchMatricula.setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        db.addListenerForSingleValueEvent(new ValueEventListener() {
                              @Override
                              public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                  if (!matricula.getText().toString().equals("")) {
                                      try {
                                          DataSnapshot matRef = dataSnapshot.child(matricula.getText().toString().toUpperCase());
                                          Boolean isLostBool = (Boolean)matRef.child("isLost").getValue();
                                          if (isLostBool) {
                                              changeIsLost.setEnabled(true);

                                              isLost.setText("¡Perdida!");
                                              String foundByStr = matRef.child("foundBy").getValue().toString();
                                              if (!foundByStr.equals("false")) {
                                                  foundBy.setText(foundByStr);
                                              }
                                              else {
                                                  foundBy.setText("Nadie :(");
                                              }

                                          }
                                          else {
                                              isLost.setText("¡Encontrada!");
                                          }
                                      }
                                      catch (Exception e) {
                                          Toast.makeText(getContext(), "Matrícula no encontrada", Toast.LENGTH_SHORT).show();
                                      }
                                  }
                                  else {
                                      Toast.makeText(getContext(), "Llena el campo de matrícula", Toast.LENGTH_SHORT).show();

                                  }
                              }

                              @Override
                              public void onCancelled(@NonNull DatabaseError databaseError) {

                              }
                        });
                    }
                }
        );

        changeIsLost.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                db.child(matricula.getText().toString().toUpperCase()).child("isLost").setValue(false);
                db.child(matricula.getText().toString().toUpperCase()).child("foundBy").setValue(false);

                // Cambiar estado en credencial
                Intent intent = new Intent(v.getContext(), MifareControl.class);
                intent.putExtra("mode", "writeExtraByteAB");
                intent.putExtra("extraByteABSector", 3);
                intent.putExtra("extraByteABSectorValue", "77");
                startActivityForResult(intent, 1);
            }
        });
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        Log.i("Received", "RC: " + requestCode + ", RESc" + resultCode);
        if (resultCode == RESULT_OK && requestCode == 1) {
            if (data.getBooleanExtra("readExtraByteABNoErrors", false)) {
                Toast.makeText(getContext(), "Estado cambiado", Toast.LENGTH_SHORT).show();
            }
        }
    }
}

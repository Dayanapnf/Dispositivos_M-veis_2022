package br.ufc.quixada.scap.DAO;

import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import br.ufc.quixada.scap.FormAddAtividade;
import br.ufc.quixada.scap.Model.Atividades;

public class AtividadesDAOFirebase implements  SCAPInterface{

    private static AtividadesDAOFirebase atividadesDAOFirebase = null;
    //private static MainActivity mainActivity;
    private static FormAddAtividade formAddAtividade;
    private FirebaseFirestore db;

    ArrayList<Atividades> listaAtividades;

    private AtividadesDAOFirebase(FormAddAtividade formAddAtividade){
        //AtividadesDAOFirebase.mainActivity = mainActivity;
        AtividadesDAOFirebase.formAddAtividade = formAddAtividade;
        listaAtividades = new ArrayList<>();
    }

    public static SCAPInterface getInstance( FormAddAtividade formAddAtividade){
        if(atividadesDAOFirebase == null){
            atividadesDAOFirebase = new AtividadesDAOFirebase(formAddAtividade);
        }

        return atividadesDAOFirebase;
    }

    @Override
    public boolean addAtividade(Atividades a) {
        Map<String, Object> atv = new HashMap<>();
        atv.put("Nome da Atividade", a.getNome_da_atividade());
        atv.put("Descricao", a.getDescricao_da_atividade());
        atv.put("Objetivo", a.getObjetivo_da_atividade());
        atv.put("Metodologia", a.getMetodologia_da_atividade());
        atv.put("Resultados", a.getResultados_da_atividade());
        atv.put("Avaliacao", a.getAvaliacao_da_atividade());

        //adicionar novo documento com ID gerado
        db.collection("Atividades").add(atv).addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
            @Override
            public void onSuccess(DocumentReference documentReference) {
                Log.d("Sucess", "DocumentSnapshot added with ID: " + documentReference.getId());
                a.setDocumentID(documentReference.getId());
                listaAtividades.add(a);
                formAddAtividade.notifyAdapter();

                Toast.makeText(formAddAtividade, "Sucess", Toast.LENGTH_SHORT).show();
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(formAddAtividade, "Error",Toast.LENGTH_SHORT).show();
            }
        });

        return true;
    }

    @Override
    public boolean editAtividade(Atividades a) {
        DocumentReference newAtividade = db.collection("Atividades").document(a.getDocumentID());
        newAtividade.update("Nome da Atividade", a.getNome_da_atividade(),
                "Descricao", a.getDescricao_da_atividade(),
                "Objetivo", a.getObjetivo_da_atividade(),
                "Metodologia", a.getMetodologia_da_atividade(),
                "Resultados", a.getResultados_da_atividade(),
                "Avaliacao", a.getAvaliacao_da_atividade()).addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void unused) {
                Toast.makeText(formAddAtividade, "Sucess", Toast.LENGTH_SHORT).show();

                for(Atividades atividade : listaAtividades ){
                    if(atividade.getDocumentID().equals(a.getDocumentID())){
                        atividade.setNome_da_atividade(a.getNome_da_atividade());
                        atividade.setDescricao_da_atividade(a.getDescricao_da_atividade());
                        atividade.setObjetivo_da_atividade(a.getObjetivo_da_atividade());
                        atividade.setMetodologia_da_atividade(a.getMetodologia_da_atividade());
                        atividade.setResultados_da_atividade(a.getResultados_da_atividade());
                        atividade.setAvaliacao_da_atividade(a.getAvaliacao_da_atividade());

                        formAddAtividade.notifyAdapter();

                        break;
                    }
                }

                formAddAtividade.notifyAdapter();
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(formAddAtividade,"Erro", Toast.LENGTH_SHORT).show();
                formAddAtividade.notifyAdapter();
            }
        });

        return false;
    }

    @Override
    public boolean deleteAtividade(int idAtividade) {
        Atividades atv = null;

        for( Atividades a : listaAtividades ){
            if( a.getId() == idAtividade ) {
                atv = a;
                break;
            }
        }

        if( atv != null ){

            final Atividades apagar = atv;

            DocumentReference deleteAtividade = db.collection("Atividades").document( atv.getDocumentID() );
            deleteAtividade.delete().addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void aVoid) {
                            Toast.makeText( formAddAtividade, "Sucess", Toast.LENGTH_LONG ).show();

                            Atividades atividadeApagar = null;
                            for( Atividades atividade : listaAtividades ){

                                if( atividade.getDocumentID().equals( apagar.getDocumentID() ) ){
                                    atividadeApagar = atividade;
                                    break;
                                }

                            }

                            if( atividadeApagar != null ) listaAtividades.remove( atividadeApagar );
                                formAddAtividade.notifyAdapter();
                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure( Exception e) {
                            Toast.makeText( formAddAtividade, "Error", Toast.LENGTH_LONG ).show();

                        }
                    });

        }

        return false;
    }

    @Override
    public Atividades getAtividade(int idAtividade) {
        return null;
    }

    @Override
    public ArrayList<Atividades> getListaAtividades() {
        listaAtividades = new ArrayList<Atividades>();

        db.collection("Atividades")
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {

                    public void onComplete( Task<QuerySnapshot> task ) {
                        if (task.isSuccessful()) {

                            for (QueryDocumentSnapshot document : task.getResult()) {

                                String nomeAtividade = document.getString( "Nome da Atividade");
                                String descricaoAtividade = document.getString( "Descricao" );
                                String objetivoAtividade = document.getString( "Objetivo" );
                                String metodologiaAtividade = document.getString( "Metodologia" );
                                String resultadosAtividade = document.getString( "Resultados" );
                                String avaliacaoAtividade = document.getString( "Avaliacao" );

                                Atividades a = new Atividades(nomeAtividade,descricaoAtividade,objetivoAtividade,metodologiaAtividade,resultadosAtividade,avaliacaoAtividade);
                                a.setDocumentID( document.getId() );

                                listaAtividades.add(a);

                            }
                        } else {
                            Log.w("TAG", "Error getting documents.", task.getException());
                        }

                        formAddAtividade.notifyAdapter();
                    }
                });


        return listaAtividades;
    }

    @Override
    public boolean deleteAll() {
        return false;
    }

    @Override
    public boolean init() {
        if(db == null) db = FirebaseFirestore.getInstance();
        return true;
    }

    @Override
    public boolean close() {
        return false;
    }

    @Override
    public boolean isStarted() {
        return false;
    }
}
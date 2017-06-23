package com.acarreos.creative.Fragments.MenuPrincipal;

import android.app.AlarmManager;
import android.app.Dialog;
import android.app.PendingIntent;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.design.widget.TextInputLayout;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.acarreos.creative.Activities.BaseActivity;
import com.acarreos.creative.BuildConfig;
import com.acarreos.creative.Constants.AppConstants;
import com.acarreos.creative.Constants.ServerConstants;
import com.acarreos.creative.Constants.UrlsServer;
import com.acarreos.creative.CustomViews.RadioButtonMaterial;
import com.acarreos.creative.Models.SessionModel;
import com.acarreos.creative.Models.UserModel;
import com.acarreos.creative.PeticionesWeb.UserPeticiones;
import com.acarreos.creative.R;
import com.acarreos.creative.Services.AlarmService;
import com.acarreos.creative.Util.ReminderSession;
import com.squareup.okhttp.OkHttpClient;
import com.wdullaer.materialdatetimepicker.date.DatePickerDialog;

import java.io.IOException;
import java.util.Calendar;
import java.util.concurrent.TimeUnit;

import fr.ganfra.materialspinner.MaterialSpinner;
import retrofit.Callback;
import retrofit.RestAdapter;
import retrofit.RetrofitError;
import retrofit.client.OkClient;
import retrofit.client.Response;

/**
 * Created by EnmanuelPc on 03/09/2015.
 */
public class FragmentLogin extends Fragment {

    private static final int MIN_LENGTH_LOGIN = 7;
    private static final int MIN_LENGTH_PASS = 5;
    private Toolbar toolbar;
    private RelativeLayout btnLogin;
    private TextView btnForgotPass;
    private FloatingActionButton btnRegister;
    private EditText editUser;
    private EditText editPass;
    LayoutInflater inflater;
    private ViewGroup container;

    public static FragmentLogin newInstance() {
        return new FragmentLogin();
    }

    public FragmentLogin() {
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        this.inflater = inflater;
        this.container = container;
        return inflater.inflate(R.layout.login, container, false);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        toolbar = (Toolbar) getView().findViewById(R.id.toolbarLogin);
        toolbar.setTitle("");
        toolbar.setContentInsetsAbsolute(0, 0);
        btnLogin = (RelativeLayout) getView().findViewById(R.id.btnLogin);
        btnForgotPass = (TextView) getView().findViewById(R.id.btnPassForget);
        btnRegister = (FloatingActionButton) getView().findViewById(R.id.btnRegister);
        if (BuildConfig.FLAVOR.equalsIgnoreCase(AppConstants.FLAVOR_TRANSPOR)) {
            btnRegister.setVisibility(View.GONE);
        }
        btnRegister.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                abrirDialogRegistrarUsuario();
            }
        });
        editPass = (EditText) getView().findViewById(R.id.passLoginEdit);
        editUser = (EditText) getView().findViewById(R.id.userLoginEdit);
        btnLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String user = editUser.getText().toString().trim();
                String pass = editPass.getText().toString().trim();
                if (user.length() > 0 && pass.length() > 0) {
                    SessionModel requestLoginInfo = new SessionModel();
                    requestLoginInfo.setLogin(user);
                    requestLoginInfo.setPassword(pass);
                    requestLoginInfo.setIdPush(new ReminderSession(getActivity()).obtenerIdPush());
                    Log.d("PUSH", requestLoginInfo.getIdPush());
                    iniciarSesionUsuario(requestLoginInfo);
                } else {
                    Snackbar.make(v, "Los campos deben estar llenos", Snackbar.LENGTH_SHORT).show();
                }
            }
        });

        btnForgotPass.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                actionResetPassword();
            }
        });
    }

    private void actionResetPassword() {
        final Dialog dialog = new Dialog(getActivity(), android.R.style.Theme_Light);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.dialog_reset_password);
        final EditText txtEmail = (EditText) dialog.findViewById(R.id.txtEmail);
        TextView btnReset = (TextView) dialog.findViewById(R.id.btnResetPassword);
        TextView btnCancel = (TextView) dialog.findViewById(R.id.btnCancel);
        btnReset.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (txtEmail.getText().toString().trim().length() < 0) {
                    Toast.makeText(getActivity(), "Debe introducir un email válido", Toast.LENGTH_SHORT).show();
                } else {
                    resetPassword(dialog, txtEmail.getText().toString().trim());
                }
            }
        });
        btnCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dialog.dismiss();
            }
        });
        dialog.show();
    }

    private void resetPassword(final Dialog dialog, String email) {
        RestAdapter restAdapter = new RestAdapter.Builder()
                .setEndpoint(UrlsServer.RUTA_SERVER_RESET)
                .build();

        UserPeticiones servicioUser = restAdapter.create(UserPeticiones.class);
        final Context context = getActivity();

        Toast.makeText(context, "Fue enviado un correo a su email con las instrucciones", Toast.LENGTH_SHORT).show();
        dialog.dismiss();

        servicioUser.resetPassword(email, new Callback<Object>() {
            @Override
            public void success(Object o, Response response) {
            }

            @Override
            public void failure(RetrofitError error) {
            }
        });
    }

    EditText textNombre;
    EditText textApellido;

    private class ObservadorEditText implements TextWatcher {

        EditText editText;

        public ObservadorEditText(EditText editText) {
            this.editText = editText;
        }

        @Override
        public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

        }

        @Override
        public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            editText.removeTextChangedListener(this);
            if (charSequence.length() != 0) {
                String cadena = charSequence.toString();
                String primeraMayus = cadena.charAt(0) + "";
                String mayus = primeraMayus.toUpperCase() + cadena.substring(1);
                editText.setText(mayus);
                editText.setSelection(editText.length());
            }
            editText.addTextChangedListener(this);
        }

        @Override
        public void afterTextChanged(Editable editable) {

        }
    }

    private void abrirDialogRegistrarUsuario() {
        //TODO hacer que los botones de registro de usuario respondan a los toques
        final Dialog dialog = new Dialog(getActivity(), android.R.style.Theme_Light);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.dialog_register_usuario);

        View radioGroup = dialog.findViewById(R.id.groupRadio);

        final EditText textFecNac = (EditText) dialog.findViewById(R.id.textFecNac);
        final EditText textLogin = (EditText) dialog.findViewById(R.id.textLogin);
        final TextInputLayout textInputLogin = (TextInputLayout) dialog.findViewById(R.id.tilLogin);
        textInputLogin.setErrorEnabled(true);
        final EditText textPass = (EditText) dialog.findViewById(R.id.textPassword);
        final TextInputLayout textInputPass = (TextInputLayout) dialog.findViewById(R.id.tilPassword);
        textInputPass.setErrorEnabled(true);
        final EditText textPasswordRentered = (EditText) dialog.findViewById(R.id.textPasswordRentered);
        final TextInputLayout textInputPassRentered = (TextInputLayout) dialog.findViewById(R.id.tilPasswordRentered);
        textInputPassRentered.setErrorEnabled(true);
        textNombre = (EditText) dialog.findViewById(R.id.textNombre);
        textNombre.addTextChangedListener(new ObservadorEditText(textNombre));
        final TextInputLayout textInputNombre = (TextInputLayout) dialog.findViewById(R.id.tilNombre);
        textInputNombre.setErrorEnabled(true);
        textApellido = (EditText) dialog.findViewById(R.id.textApellido);
        textApellido.addTextChangedListener(new ObservadorEditText(textApellido));
        final TextInputLayout textInputApellido = (TextInputLayout) dialog.findViewById(R.id.tilApellido);
        textInputApellido.setErrorEnabled(true);
        final EditText textEmail = (EditText) dialog.findViewById(R.id.textEmail);
        final TextInputLayout textInputEmail = (TextInputLayout) dialog.findViewById(R.id.tilEmail);
        textInputEmail.setErrorEnabled(true);
        final EditText textTelefono = (EditText) dialog.findViewById(R.id.textTelefono);
        final TextInputLayout textInputTelefono = (TextInputLayout) dialog.findViewById(R.id.tilTelefono);
        textInputTelefono.setErrorEnabled(true);
        final RadioButtonMaterial rbtOpcCli = (RadioButtonMaterial) dialog.findViewById(R.id.rbtOpcCliente);
        final RadioButtonMaterial rbtOpcTrans = (RadioButtonMaterial) dialog.findViewById(R.id.rbtOpcTranspor);

        final EditText textDni = (EditText) dialog.findViewById(R.id.textDni);
        final TextInputLayout textInputDni = (TextInputLayout) dialog.findViewById(R.id.tilDni);
        textInputDni.setErrorEnabled(true);

        final MaterialSpinner spinnerTipoDni = (MaterialSpinner) dialog.findViewById(R.id.spinnerTipoDni);
        spinnerTipoDni.setPaddingSafe(0, 0, 0, 0);
        String[] arrayTipoDni = new String[4];
        arrayTipoDni[0] = "Cédula de Identidad";
        arrayTipoDni[1] = "Código de Entidad Pública";
        arrayTipoDni[2] = "Registro Único Contribuyente";
        arrayTipoDni[3] = "Pasaporte";
        ArrayAdapter<String> adapterTipoDniList = new ArrayAdapter<String>(getActivity(), android.R.layout.simple_spinner_item, arrayTipoDni);
        adapterTipoDniList.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerTipoDni.setAdapter(adapterTipoDniList);
        spinnerTipoDni.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (position == -1) {
                    ((TextView) parent.getChildAt(0)).setTextColor(getActivity().getResources().getColor(R.color.gray_text));
                } else {
                    ((TextView) parent.getChildAt(0)).setTextColor(Color.BLACK);
                }
                int indice = position + 1;
                switch (indice) {
                    case UserModel.TIPO_CEDULA:
                        textInputDni.setHint("Ingrese cédula");
                        break;
                    case UserModel.TIPO_CODIGO_ENTIDAD_PUBLICA:
                        textInputDni.setHint("Ingrese código de entidad pública");
                        break;
                    case UserModel.TIPO_REGISTRO_UNICO_CONTRIBUYENTE:
                        textInputDni.setHint("Ingrese registro único contribuyente");
                        break;
                    case UserModel.TIPO_PASAPORTE:
                        textInputDni.setHint("Ingrese número de pasaporte");
                        break;
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });

        final EditText textNumSeguridad = (EditText) dialog.findViewById(R.id.textNumSeguridad);
        final TextInputLayout textInputNumSeguridad = (TextInputLayout) dialog.findViewById(R.id.tilNumSeguridad);
        textInputNumSeguridad.setErrorEnabled(true);

        ImageView btnHelpNumSeg = (ImageView) dialog.findViewById(R.id.btnHelpNumSeg);
        btnHelpNumSeg.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                final AlertDialog.Builder alertadd = new AlertDialog.Builder(
                        getActivity());
                LayoutInflater factory = LayoutInflater.from(getActivity());
                final View viewDialog = factory.inflate(R.layout.dialog_help_num_seg, null);
                alertadd.setView(viewDialog);
                alertadd.setNeutralButton("OK", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dlg, int sumthin) {
                    }
                });

                alertadd.show();
            }
        });

        final MaterialSpinner spinnerTipoLicencia = (MaterialSpinner) dialog.findViewById(R.id.spinnerTipoLicencia);
        spinnerTipoLicencia.setPaddingSafe(0, 0, 0, 0);
        String[] arrayTipoLicencia = new String[12];
        arrayTipoLicencia[0] = "A";
        arrayTipoLicencia[1] = "B";
        arrayTipoLicencia[2] = "C";
        arrayTipoLicencia[3] = "D";
        arrayTipoLicencia[4] = "E";
        arrayTipoLicencia[5] = "E1 a";
        arrayTipoLicencia[6] = "E2 b";
        arrayTipoLicencia[7] = "E3 c";
        arrayTipoLicencia[8] = "F";
        arrayTipoLicencia[9] = "G";
        arrayTipoLicencia[10] = "H";
        arrayTipoLicencia[11] = "I";
        ArrayAdapter<String> adapterTipoLicenciaList = new ArrayAdapter<String>(getActivity(), android.R.layout.simple_spinner_item, arrayTipoLicencia);
        adapterTipoLicenciaList.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerTipoLicencia.setAdapter(adapterTipoLicenciaList);
        spinnerTipoLicencia.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (position == -1) {
                    ((TextView) parent.getChildAt(0)).setTextColor(getActivity().getResources().getColor(R.color.gray_text));
                } else {
                    ((TextView) parent.getChildAt(0)).setTextColor(Color.BLACK);
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });


        ImageView btnClose = (ImageView) dialog.findViewById(R.id.btnClose);
        TextView btnRegistrar = (TextView) dialog.findViewById(R.id.btnRegistrar);
        textFecNac.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (event.getAction() == MotionEvent.ACTION_DOWN) {
                    //TODO que cuando te vuelvas a meter al calendario aparezca la fecha que ya tenía que ya tenía
                    Calendar now = Calendar.getInstance();
                    DatePickerDialog dpd = DatePickerDialog.newInstance(
                            new DatePickerDialog.OnDateSetListener() {
                                @Override
                                public void onDateSet(DatePickerDialog datePickerDialog, int year, int month, int day) {
                                    textFecNac.setText(year + "/" + (month + 1) + "/" + day);
                                }
                            },
                            now.get(Calendar.YEAR),
                            now.get(Calendar.MONTH),
                            now.get(Calendar.DAY_OF_MONTH)
                    );
                    dpd.showYearPickerFirst(true);
                    dpd.show(getActivity().getFragmentManager(), "DatepickerdialogHola");
                    return true;
                }
                return false;
            }
        });
        btnClose.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
            }
        });
        textDni.setEnabled(false);
        spinnerTipoDni.setEnabled(false);
        spinnerTipoLicencia.setEnabled(false);
        textNumSeguridad.setEnabled(false);
        rbtOpcCli.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean checked) {
                if (!checked) {
                    textDni.setEnabled(true);
                    spinnerTipoDni.setEnabled(true);
                    spinnerTipoLicencia.setEnabled(true);
                    textNumSeguridad.setEnabled(true);
                } else {
                    textDni.setEnabled(false);
                    spinnerTipoDni.setEnabled(false);
                    spinnerTipoLicencia.setEnabled(false);
                    textNumSeguridad.setEnabled(false);
                }
            }
        });
        btnRegistrar.setOnClickListener(new View.OnClickListener() {
                                            @Override
                                            public void onClick(View v) {
                                                boolean valido = true;
                                                textInputLogin.setError(null);
                                                textInputPass.setError(null);
                                                textInputPassRentered.setError(null);
                                                textInputNombre.setError(null);
                                                textInputApellido.setError(null);
                                                textInputEmail.setError(null);
                                                textFecNac.setError(null);
                                                textTelefono.setError(null);
                                                if (textLogin.getText().toString().length() < MIN_LENGTH_LOGIN) {
                                                    textInputLogin.setError("Debe tener al menos siete caracteres");
                                                    valido = false;
                                                } else if (textPass.getText().toString().length() < MIN_LENGTH_PASS) {
                                                    textInputPass.setError("Debe tener al menos cinco caracteres");
                                                    valido = false;
                                                } else if (textPass.getText().toString().compareTo(textPasswordRentered.getText().toString()) != 0) {
                                                    textInputPassRentered.setError("Las contraseñas no coinciden");
                                                    valido = false;
                                                } else if (textNombre.getText().toString().length() == 0) {
                                                    textInputNombre.setError("Este campo es obligatorio");
                                                    valido = false;
                                                } else if (textApellido.getText().toString().length() == 0) {
                                                    textInputApellido.setError("Este campo es obligatorio");
                                                    valido = false;
                                                } else if (textEmail.getText().toString().length() == 0) {
                                                    textInputEmail.setError("Este campo es obligatorio");
                                                    valido = false;
                                                } else if (textFecNac.getText().toString().length() == 0) {
                                                    textFecNac.setError("Este campo es obligatorio");
                                                    valido = false;
                                                } else if (textTelefono.getText().toString().length() == 0) {
                                                    textTelefono.setError("Este campo es obligatorio");
                                                    valido = false;
                                                } else if (!rbtOpcCli.isChecked()) {
                                                    if (spinnerTipoDni.getSelectedItemPosition() == -1) {
                                                        Toast.makeText(getActivity(), "Debe seleccionar un tipo de identificación", Toast.LENGTH_SHORT).show();
                                                        valido = false;
                                                    } else if (textDni.getText().toString().length() == 0) {
                                                        textDni.setError("Este campo es obligatorio");
                                                        valido = false;
                                                    } else if (spinnerTipoLicencia.getSelectedItemPosition() == -1) {
                                                        Toast.makeText(getActivity(), "Debe seleccionar un tipo de licencia", Toast.LENGTH_SHORT).show();
                                                        valido = false;
                                                    } else if (textNumSeguridad.getText().toString().length() == 0) {
                                                        textNumSeguridad.setError("Este campo es obligatorio");
                                                        valido = false;
                                                    }
                                                }
                                                if (valido) {
                                                    UserModel userInfo = new UserModel();
                                                    userInfo.setNombre(textNombre.getText().toString());
                                                    userInfo.setApellido(textApellido.getText().toString());
                                                    userInfo.setLogin(textLogin.getText().toString());
                                                    userInfo.setPassword(textPass.getText().toString());
                                                    userInfo.setEmail(textEmail.getText().toString());
                                                    userInfo.setTelefono(textTelefono.getText().toString());
                                                    userInfo.setFecha_nac(textFecNac.getText().toString());
                                                    ReminderSession reminderSession = new ReminderSession(getActivity());
                                                    if (reminderSession.obtenerIdPush() != null) {
                                                        userInfo.setIdPush(new ReminderSession(getActivity()).obtenerIdPush());
                                                    }
                                                    if (BuildConfig.FLAVOR.equalsIgnoreCase(AppConstants.FLAVOR_CLIENT)) {
                                                        userInfo.setTipo_user_id(UserModel.TIPO_CLIENTE);
                                                    } else {
                                                        if (rbtOpcCli.isChecked()) {
                                                            userInfo.setTipo_user_id(UserModel.TIPO_CLIENTE);
                                                        } else {
                                                            userInfo.setTipoDni(spinnerTipoDni.getSelectedItemPosition());
                                                            userInfo.setDni(textDni.getText().toString().trim());
                                                            userInfo.setTipoLicencia(((String) spinnerTipoLicencia.getSelectedItem()).trim());
                                                            userInfo.setNumSeguridad(textNumSeguridad.getText().toString());
                                                            userInfo.setTipo_user_id(UserModel.TIPO_TRANSPORTISTA);
                                                        }
                                                    }
                                                    registrarUsuario(userInfo, dialog);
                                                }
                                            }
                                        }
        );

        if (!BuildConfig.FLAVOR.equalsIgnoreCase(AppConstants.FLAVOR_ADMIN)) {
            radioGroup.setVisibility(View.GONE);
            dialog.findViewById(R.id.layoutDni).setVisibility(View.GONE);
            dialog.findViewById(R.id.layoutDniType).setVisibility(View.GONE);
            dialog.findViewById(R.id.layoutSecurityNumber).setVisibility(View.GONE);
            dialog.findViewById(R.id.layoutTipoLicencia).setVisibility(View.GONE);
        }
        dialog.show();
    }

    private void registrarUsuario(final UserModel userInfo, final Dialog dialog) {
        //TODO Capturar los errores de datos desde el servidor
        LayoutInflater inflater = (LayoutInflater) getActivity().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        final View screenLoading = inflater.inflate(R.layout.layout_loading_screen, container, false);
        dialog.hide();
        container.addView(screenLoading);
        //final View screenLoading = inflater.inflate(R.layout.layout_loading_screen, null);
        RestAdapter restAdapter = new RestAdapter.Builder()
                .setEndpoint(UrlsServer.RUTA_SERVER)
                .build();


        UserPeticiones servicioRegistroUser = restAdapter.create(UserPeticiones.class);

        servicioRegistroUser.registrarUsuario(userInfo, new Callback<SessionModel>() {
            @Override
            public void success(SessionModel sessionModel, Response response) {
                screenLoading.setVisibility(View.GONE);
                dialog.dismiss();
                ReminderSession reminderSession = new ReminderSession(getActivity());
                reminderSession.guardarSession(sessionModel);
                BaseActivity activity = (BaseActivity) getActivity();
                activity.ingresar(sessionModel.getUser());
            }

            @Override
            public void failure(RetrofitError error) {
                //TODO manejar los errores del servidor
                dialog.show();
                screenLoading.setVisibility(View.GONE);
                Log.d("ERROR", error.toString());
                Log.d("ERROR", error.getMessage());
                if (error.getResponse() != null) {
                    if (error.getResponse().getStatus() == ServerConstants.STATUS_UNAUTHORIZED) {
                        Snackbar.make(btnLogin, "Usuario inválido", Snackbar.LENGTH_LONG).show();
                    } else {
                        try {
                            String errorCadena = convertStreamToString(error.getResponse().getBody().in());
                            errorCadena = errorCadena.replaceAll("\\{", "");
                            errorCadena = errorCadena.replaceAll("\\[", "");
                            errorCadena = errorCadena.replaceAll("\\}", "");
                            errorCadena = errorCadena.replaceAll("\\]", "");
                            errorCadena = errorCadena.replaceAll("\"", "");
                            if (errorCadena.contains("error")) {
                                Toast.makeText(getActivity(), errorCadena, Toast.LENGTH_LONG).show();
                            } else {
                                dialog.dismiss();
                                Snackbar.make(btnLogin, "Problemas de conexión", Snackbar.LENGTH_LONG).show();
                            }
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                }
            }
        });
    }

    static String convertStreamToString(java.io.InputStream is) {
        java.util.Scanner s = new java.util.Scanner(is).useDelimiter("\\A");
        return s.hasNext() ? s.next() : "";
    }

    private void iniciarSesionUsuario(SessionModel requestLoginInfo) {
        LayoutInflater inflater = (LayoutInflater) getActivity().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        final View screenLoading = inflater.inflate(R.layout.layout_loading_screen, container, false);
        container.addView(screenLoading);
        OkHttpClient cliente = new OkHttpClient();
        cliente.setConnectTimeout(60, TimeUnit.SECONDS);
        cliente.setReadTimeout(60, TimeUnit.SECONDS);
        RestAdapter restAdapter = new RestAdapter.Builder()
                .setEndpoint(UrlsServer.RUTA_SERVER)
                .setClient(new OkClient(cliente))
                .build();

        UserPeticiones servicioIniciarSesion = restAdapter.create(UserPeticiones.class);

        final BaseActivity context = (BaseActivity) getActivity();

        servicioIniciarSesion.iniciarSession(requestLoginInfo, new Callback<SessionModel>() {
            @Override
            public void success(SessionModel sessionModel, Response response) {
                boolean isValid = true;
                if (BuildConfig.FLAVOR.equalsIgnoreCase(AppConstants.FLAVOR_CLIENT)) {
                    isValid = sessionModel.getUser().getTipo_user_id() == UserModel.TIPO_CLIENTE;
                } else if (BuildConfig.FLAVOR.equalsIgnoreCase(AppConstants.FLAVOR_TRANSPOR)) {
                    isValid = sessionModel.getUser().getTipo_user_id() == UserModel.TIPO_TRANSPORTISTA;
                }
                screenLoading.setVisibility(View.GONE);
                if (isValid) {
                    ReminderSession reminderSession = new ReminderSession(context);
                    reminderSession.guardarSession(sessionModel);
                    activarAlarma(context);
                    context.ingresar(sessionModel.getUser());
                } else {
                    Snackbar.make(btnLogin, "Usuario inválido", Snackbar.LENGTH_LONG).show();
                }
            }

            @Override
            public void failure(RetrofitError error) {
                screenLoading.setVisibility(View.GONE);
                Log.d("ERROR", error.toString());
                if (error.getResponse() != null) {
                    if (error.getResponse().getStatus() == ServerConstants.STATUS_UNAUTHORIZED) {
                        Snackbar.make(btnLogin, "Usuario inválido", Snackbar.LENGTH_LONG).show();
                    } else {
                        Snackbar.make(btnLogin, "Problemas de conexión", Snackbar.LENGTH_LONG).show();
                    }
                } else {
                    Snackbar.make(btnLogin, "Problemas de conexión", Snackbar.LENGTH_LONG).show();
                }
            }
        });
    }

    private void activarAlarma(BaseActivity context) {
        Intent myIntent = new Intent(context, AlarmService.class);
        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        PendingIntent pendingIntent = PendingIntent.getService(context, AlarmService.ALARM_ID, myIntent, 0);

        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.HOUR, 0);
        calendar.set(Calendar.AM_PM, Calendar.AM);
        calendar.add(Calendar.DAY_OF_MONTH, 1);

        alarmManager.setRepeating(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(), 1000 * 60 * 60 * 24, pendingIntent);
    }
}

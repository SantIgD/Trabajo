/*
 * This file is auto-generated.  DO NOT MODIFY.
 */
package coop.tecso;
public interface IUDAAService extends android.os.IInterface
{
/** Local-side IPC implementation stub class. */
public static abstract class Stub extends android.os.Binder implements coop.tecso.IUDAAService
{
private static final java.lang.String DESCRIPTOR = "coop.tecso.IUDAAService";
/** Construct the stub at attach it to the interface. */
public Stub()
{
this.attachInterface(this, DESCRIPTOR);
}
/**
 * Cast an IBinder object into an coop.tecso.IUDAAService interface,
 * generating a proxy if needed.
 */
public static coop.tecso.IUDAAService asInterface(android.os.IBinder obj)
{
if ((obj==null)) {
return null;
}
android.os.IInterface iin = obj.queryLocalInterface(DESCRIPTOR);
if (((iin!=null)&&(iin instanceof coop.tecso.IUDAAService))) {
return ((coop.tecso.IUDAAService)iin);
}
return new coop.tecso.IUDAAService.Stub.Proxy(obj);
}
@Override public android.os.IBinder asBinder()
{
return this;
}
@Override public boolean onTransact(int code, android.os.Parcel data, android.os.Parcel reply, int flags) throws android.os.RemoteException
{
java.lang.String descriptor = DESCRIPTOR;
switch (code)
{
case INTERFACE_TRANSACTION:
{
reply.writeString(descriptor);
return true;
}
case TRANSACTION_getCurrentUser:
{
data.enforceInterface(descriptor);
java.lang.String _result = this.getCurrentUser();
reply.writeNoException();
reply.writeString(_result);
return true;
}
case TRANSACTION_getServerURL:
{
data.enforceInterface(descriptor);
java.lang.String _result = this.getServerURL();
reply.writeNoException();
reply.writeString(_result);
return true;
}
case TRANSACTION_isTransTypePartial:
{
data.enforceInterface(descriptor);
boolean _result = this.isTransTypePartial();
reply.writeNoException();
reply.writeInt(((_result)?(1):(0)));
return true;
}
case TRANSACTION_getAplicacionPerfilById:
{
data.enforceInterface(descriptor);
int _arg0;
_arg0 = data.readInt();
java.lang.String _result = this.getAplicacionPerfilById(_arg0);
reply.writeNoException();
reply.writeString(_result);
return true;
}
case TRANSACTION_sync:
{
data.enforceInterface(descriptor);
java.lang.String _arg0;
_arg0 = data.readString();
java.lang.String _arg1;
_arg1 = data.readString();
int _arg2;
_arg2 = data.readInt();
java.lang.String _result = this.sync(_arg0, _arg1, _arg2);
reply.writeNoException();
reply.writeString(_result);
return true;
}
case TRANSACTION_fetchUser:
{
data.enforceInterface(descriptor);
int _arg0;
_arg0 = data.readInt();
java.lang.String _result = this.fetchUser(_arg0);
reply.writeNoException();
reply.writeString(_result);
return true;
}
case TRANSACTION_login:
{
data.enforceInterface(descriptor);
java.lang.String _arg0;
_arg0 = data.readString();
java.lang.String _arg1;
_arg1 = data.readString();
java.lang.String _result = this.login(_arg0, _arg1);
reply.writeNoException();
reply.writeString(_result);
return true;
}
case TRANSACTION_getDispositivoMovil:
{
data.enforceInterface(descriptor);
java.lang.String _result = this.getDispositivoMovil();
reply.writeNoException();
reply.writeString(_result);
return true;
}
case TRANSACTION_getNotificacionById:
{
data.enforceInterface(descriptor);
int _arg0;
_arg0 = data.readInt();
java.lang.String _result = this.getNotificacionById(_arg0);
reply.writeNoException();
reply.writeString(_result);
return true;
}
case TRANSACTION_changeSession:
{
data.enforceInterface(descriptor);
java.lang.String _arg0;
_arg0 = data.readString();
java.lang.String _arg1;
_arg1 = data.readString();
this.changeSession(_arg0, _arg1);
reply.writeNoException();
return true;
}
case TRANSACTION_hasAccess:
{
data.enforceInterface(descriptor);
int _arg0;
_arg0 = data.readInt();
java.lang.String _arg1;
_arg1 = data.readString();
boolean _result = this.hasAccess(_arg0, _arg1);
reply.writeNoException();
reply.writeInt(((_result)?(1):(0)));
return true;
}
case TRANSACTION_getIdAplicacionPerfilDefaultBy:
{
data.enforceInterface(descriptor);
java.lang.String _arg0;
_arg0 = data.readString();
java.lang.String _result = this.getIdAplicacionPerfilDefaultBy(_arg0);
reply.writeNoException();
reply.writeString(_result);
return true;
}
case TRANSACTION_getCampoBy:
{
data.enforceInterface(descriptor);
int _arg0;
_arg0 = data.readInt();
int _arg1;
_arg1 = data.readInt();
java.lang.String _result = this.getCampoBy(_arg0, _arg1);
reply.writeNoException();
reply.writeString(_result);
return true;
}
case TRANSACTION_getAplPerfilSeccionCampoById:
{
data.enforceInterface(descriptor);
int _arg0;
_arg0 = data.readInt();
java.lang.String _result = this.getAplPerfilSeccionCampoById(_arg0);
reply.writeNoException();
reply.writeString(_result);
return true;
}
case TRANSACTION_sendError:
{
data.enforceInterface(descriptor);
int _arg0;
_arg0 = data.readInt();
this.sendError(_arg0);
reply.writeNoException();
return true;
}
case TRANSACTION_generateReport:
{
data.enforceInterface(descriptor);
java.lang.String _arg0;
_arg0 = data.readString();
java.lang.String _arg1;
_arg1 = data.readString();
java.lang.String _arg2;
_arg2 = data.readString();
java.lang.String _result = this.generateReport(_arg0, _arg1, _arg2);
reply.writeNoException();
reply.writeString(_result);
return true;
}
case TRANSACTION_getListBinarioPathBy:
{
data.enforceInterface(descriptor);
java.lang.String _arg0;
_arg0 = data.readString();
java.lang.String _arg1;
_arg1 = data.readString();
java.lang.String _result = this.getListBinarioPathBy(_arg0, _arg1);
reply.writeNoException();
reply.writeString(_result);
return true;
}
case TRANSACTION_exportDataToFile:
{
data.enforceInterface(descriptor);
java.lang.String _arg0;
_arg0 = data.readString();
java.lang.String _result = this.exportDataToFile(_arg0);
reply.writeNoException();
reply.writeString(_result);
return true;
}
case TRANSACTION_getLastAplicacionBinarioVersionByCodigoAplicacion:
{
data.enforceInterface(descriptor);
java.lang.String _arg0;
_arg0 = data.readString();
java.lang.String _result = this.getLastAplicacionBinarioVersionByCodigoAplicacion(_arg0);
reply.writeNoException();
reply.writeString(_result);
return true;
}
case TRANSACTION_confirmForceUpdate:
{
data.enforceInterface(descriptor);
java.lang.String _arg0;
_arg0 = data.readString();
boolean _arg1;
_arg1 = (0!=data.readInt());
java.lang.String _result = this.confirmForceUpdate(_arg0, _arg1);
reply.writeNoException();
reply.writeString(_result);
return true;
}
case TRANSACTION_updateApplicationSync:
{
data.enforceInterface(descriptor);
java.lang.String _arg0;
_arg0 = data.readString();
this.updateApplicationSync(_arg0);
reply.writeNoException();
return true;
}
case TRANSACTION_rawQueryList:
{
data.enforceInterface(descriptor);
java.lang.String _arg0;
_arg0 = data.readString();
java.lang.String _arg1;
_arg1 = data.readString();
java.lang.String _result = this.rawQueryList(_arg0, _arg1);
reply.writeNoException();
reply.writeString(_result);
return true;
}
case TRANSACTION_rawQueryCiudades:
{
data.enforceInterface(descriptor);
java.lang.String _arg0;
_arg0 = data.readString();
java.lang.String _arg1;
_arg1 = data.readString();
java.lang.String _result = this.rawQueryCiudades(_arg0, _arg1);
reply.writeNoException();
reply.writeString(_result);
return true;
}
case TRANSACTION_query:
{
data.enforceInterface(descriptor);
java.lang.String _arg0;
_arg0 = data.readString();
java.lang.String _arg1;
_arg1 = data.readString();
java.lang.String _arg2;
_arg2 = data.readString();
java.lang.String _arg3;
_arg3 = data.readString();
java.lang.String _arg4;
_arg4 = data.readString();
java.lang.String _arg5;
_arg5 = data.readString();
java.lang.String _arg6;
_arg6 = data.readString();
java.lang.String _result = this.query(_arg0, _arg1, _arg2, _arg3, _arg4, _arg5, _arg6);
reply.writeNoException();
reply.writeString(_result);
return true;
}
default:
{
return super.onTransact(code, data, reply, flags);
}
}
}
private static class Proxy implements coop.tecso.IUDAAService
{
private android.os.IBinder mRemote;
Proxy(android.os.IBinder remote)
{
mRemote = remote;
}
@Override public android.os.IBinder asBinder()
{
return mRemote;
}
public java.lang.String getInterfaceDescriptor()
{
return DESCRIPTOR;
}
@Override public java.lang.String getCurrentUser() throws android.os.RemoteException
{
android.os.Parcel _data = android.os.Parcel.obtain();
android.os.Parcel _reply = android.os.Parcel.obtain();
java.lang.String _result;
try {
_data.writeInterfaceToken(DESCRIPTOR);
mRemote.transact(Stub.TRANSACTION_getCurrentUser, _data, _reply, 0);
_reply.readException();
_result = _reply.readString();
}
finally {
_reply.recycle();
_data.recycle();
}
return _result;
}
@Override public java.lang.String getServerURL() throws android.os.RemoteException
{
android.os.Parcel _data = android.os.Parcel.obtain();
android.os.Parcel _reply = android.os.Parcel.obtain();
java.lang.String _result;
try {
_data.writeInterfaceToken(DESCRIPTOR);
mRemote.transact(Stub.TRANSACTION_getServerURL, _data, _reply, 0);
_reply.readException();
_result = _reply.readString();
}
finally {
_reply.recycle();
_data.recycle();
}
return _result;
}
@Override public boolean isTransTypePartial() throws android.os.RemoteException
{
android.os.Parcel _data = android.os.Parcel.obtain();
android.os.Parcel _reply = android.os.Parcel.obtain();
boolean _result;
try {
_data.writeInterfaceToken(DESCRIPTOR);
mRemote.transact(Stub.TRANSACTION_isTransTypePartial, _data, _reply, 0);
_reply.readException();
_result = (0!=_reply.readInt());
}
finally {
_reply.recycle();
_data.recycle();
}
return _result;
}
@Override public java.lang.String getAplicacionPerfilById(int aplicacionPerfilId) throws android.os.RemoteException
{
android.os.Parcel _data = android.os.Parcel.obtain();
android.os.Parcel _reply = android.os.Parcel.obtain();
java.lang.String _result;
try {
_data.writeInterfaceToken(DESCRIPTOR);
_data.writeInt(aplicacionPerfilId);
mRemote.transact(Stub.TRANSACTION_getAplicacionPerfilById, _data, _reply, 0);
_reply.readException();
_result = _reply.readString();
}
finally {
_reply.recycle();
_data.recycle();
}
return _result;
}
@Override public java.lang.String sync(java.lang.String clazz, java.lang.String table, int version) throws android.os.RemoteException
{
android.os.Parcel _data = android.os.Parcel.obtain();
android.os.Parcel _reply = android.os.Parcel.obtain();
java.lang.String _result;
try {
_data.writeInterfaceToken(DESCRIPTOR);
_data.writeString(clazz);
_data.writeString(table);
_data.writeInt(version);
mRemote.transact(Stub.TRANSACTION_sync, _data, _reply, 0);
_reply.readException();
_result = _reply.readString();
}
finally {
_reply.recycle();
_data.recycle();
}
return _result;
}
@Override public java.lang.String fetchUser(int userID) throws android.os.RemoteException
{
android.os.Parcel _data = android.os.Parcel.obtain();
android.os.Parcel _reply = android.os.Parcel.obtain();
java.lang.String _result;
try {
_data.writeInterfaceToken(DESCRIPTOR);
_data.writeInt(userID);
mRemote.transact(Stub.TRANSACTION_fetchUser, _data, _reply, 0);
_reply.readException();
_result = _reply.readString();
}
finally {
_reply.recycle();
_data.recycle();
}
return _result;
}
@Override public java.lang.String login(java.lang.String username, java.lang.String password) throws android.os.RemoteException
{
android.os.Parcel _data = android.os.Parcel.obtain();
android.os.Parcel _reply = android.os.Parcel.obtain();
java.lang.String _result;
try {
_data.writeInterfaceToken(DESCRIPTOR);
_data.writeString(username);
_data.writeString(password);
mRemote.transact(Stub.TRANSACTION_login, _data, _reply, 0);
_reply.readException();
_result = _reply.readString();
}
finally {
_reply.recycle();
_data.recycle();
}
return _result;
}
@Override public java.lang.String getDispositivoMovil() throws android.os.RemoteException
{
android.os.Parcel _data = android.os.Parcel.obtain();
android.os.Parcel _reply = android.os.Parcel.obtain();
java.lang.String _result;
try {
_data.writeInterfaceToken(DESCRIPTOR);
mRemote.transact(Stub.TRANSACTION_getDispositivoMovil, _data, _reply, 0);
_reply.readException();
_result = _reply.readString();
}
finally {
_reply.recycle();
_data.recycle();
}
return _result;
}
@Override public java.lang.String getNotificacionById(int notificacionID) throws android.os.RemoteException
{
android.os.Parcel _data = android.os.Parcel.obtain();
android.os.Parcel _reply = android.os.Parcel.obtain();
java.lang.String _result;
try {
_data.writeInterfaceToken(DESCRIPTOR);
_data.writeInt(notificacionID);
mRemote.transact(Stub.TRANSACTION_getNotificacionById, _data, _reply, 0);
_reply.readException();
_result = _reply.readString();
}
finally {
_reply.recycle();
_data.recycle();
}
return _result;
}
@Override public void changeSession(java.lang.String username, java.lang.String password) throws android.os.RemoteException
{
android.os.Parcel _data = android.os.Parcel.obtain();
android.os.Parcel _reply = android.os.Parcel.obtain();
try {
_data.writeInterfaceToken(DESCRIPTOR);
_data.writeString(username);
_data.writeString(password);
mRemote.transact(Stub.TRANSACTION_changeSession, _data, _reply, 0);
_reply.readException();
}
finally {
_reply.recycle();
_data.recycle();
}
}
@Override public boolean hasAccess(int usuarioID, java.lang.String codAplicacion) throws android.os.RemoteException
{
android.os.Parcel _data = android.os.Parcel.obtain();
android.os.Parcel _reply = android.os.Parcel.obtain();
boolean _result;
try {
_data.writeInterfaceToken(DESCRIPTOR);
_data.writeInt(usuarioID);
_data.writeString(codAplicacion);
mRemote.transact(Stub.TRANSACTION_hasAccess, _data, _reply, 0);
_reply.readException();
_result = (0!=_reply.readInt());
}
finally {
_reply.recycle();
_data.recycle();
}
return _result;
}
@Override public java.lang.String getIdAplicacionPerfilDefaultBy(java.lang.String codAplicacion) throws android.os.RemoteException
{
android.os.Parcel _data = android.os.Parcel.obtain();
android.os.Parcel _reply = android.os.Parcel.obtain();
java.lang.String _result;
try {
_data.writeInterfaceToken(DESCRIPTOR);
_data.writeString(codAplicacion);
mRemote.transact(Stub.TRANSACTION_getIdAplicacionPerfilDefaultBy, _data, _reply, 0);
_reply.readException();
_result = _reply.readString();
}
finally {
_reply.recycle();
_data.recycle();
}
return _result;
}
@Override public java.lang.String getCampoBy(int campoId, int aplicacionPerfilId) throws android.os.RemoteException
{
android.os.Parcel _data = android.os.Parcel.obtain();
android.os.Parcel _reply = android.os.Parcel.obtain();
java.lang.String _result;
try {
_data.writeInterfaceToken(DESCRIPTOR);
_data.writeInt(campoId);
_data.writeInt(aplicacionPerfilId);
mRemote.transact(Stub.TRANSACTION_getCampoBy, _data, _reply, 0);
_reply.readException();
_result = _reply.readString();
}
finally {
_reply.recycle();
_data.recycle();
}
return _result;
}
@Override public java.lang.String getAplPerfilSeccionCampoById(int aplPerfilSeccionCampoId) throws android.os.RemoteException
{
android.os.Parcel _data = android.os.Parcel.obtain();
android.os.Parcel _reply = android.os.Parcel.obtain();
java.lang.String _result;
try {
_data.writeInterfaceToken(DESCRIPTOR);
_data.writeInt(aplPerfilSeccionCampoId);
mRemote.transact(Stub.TRANSACTION_getAplPerfilSeccionCampoById, _data, _reply, 0);
_reply.readException();
_result = _reply.readString();
}
finally {
_reply.recycle();
_data.recycle();
}
return _result;
}
@Override public void sendError(int tipoReg) throws android.os.RemoteException
{
android.os.Parcel _data = android.os.Parcel.obtain();
android.os.Parcel _reply = android.os.Parcel.obtain();
try {
_data.writeInterfaceToken(DESCRIPTOR);
_data.writeInt(tipoReg);
mRemote.transact(Stub.TRANSACTION_sendError, _data, _reply, 0);
_reply.readException();
}
finally {
_reply.recycle();
_data.recycle();
}
}
@Override public java.lang.String generateReport(java.lang.String jsonData, java.lang.String jsonSection, java.lang.String jsonTemplate) throws android.os.RemoteException
{
android.os.Parcel _data = android.os.Parcel.obtain();
android.os.Parcel _reply = android.os.Parcel.obtain();
java.lang.String _result;
try {
_data.writeInterfaceToken(DESCRIPTOR);
_data.writeString(jsonData);
_data.writeString(jsonSection);
_data.writeString(jsonTemplate);
mRemote.transact(Stub.TRANSACTION_generateReport, _data, _reply, 0);
_reply.readException();
_result = _reply.readString();
}
finally {
_reply.recycle();
_data.recycle();
}
return _result;
}
@Override public java.lang.String getListBinarioPathBy(java.lang.String codAplicacion, java.lang.String tipoBinario) throws android.os.RemoteException
{
android.os.Parcel _data = android.os.Parcel.obtain();
android.os.Parcel _reply = android.os.Parcel.obtain();
java.lang.String _result;
try {
_data.writeInterfaceToken(DESCRIPTOR);
_data.writeString(codAplicacion);
_data.writeString(tipoBinario);
mRemote.transact(Stub.TRANSACTION_getListBinarioPathBy, _data, _reply, 0);
_reply.readException();
_result = _reply.readString();
}
finally {
_reply.recycle();
_data.recycle();
}
return _result;
}
@Override public java.lang.String exportDataToFile(java.lang.String jSonData) throws android.os.RemoteException
{
android.os.Parcel _data = android.os.Parcel.obtain();
android.os.Parcel _reply = android.os.Parcel.obtain();
java.lang.String _result;
try {
_data.writeInterfaceToken(DESCRIPTOR);
_data.writeString(jSonData);
mRemote.transact(Stub.TRANSACTION_exportDataToFile, _data, _reply, 0);
_reply.readException();
_result = _reply.readString();
}
finally {
_reply.recycle();
_data.recycle();
}
return _result;
}
@Override public java.lang.String getLastAplicacionBinarioVersionByCodigoAplicacion(java.lang.String appCode) throws android.os.RemoteException
{
android.os.Parcel _data = android.os.Parcel.obtain();
android.os.Parcel _reply = android.os.Parcel.obtain();
java.lang.String _result;
try {
_data.writeInterfaceToken(DESCRIPTOR);
_data.writeString(appCode);
mRemote.transact(Stub.TRANSACTION_getLastAplicacionBinarioVersionByCodigoAplicacion, _data, _reply, 0);
_reply.readException();
_result = _reply.readString();
}
finally {
_reply.recycle();
_data.recycle();
}
return _result;
}
@Override public java.lang.String confirmForceUpdate(java.lang.String appCode, boolean lastAppToUpdate) throws android.os.RemoteException
{
android.os.Parcel _data = android.os.Parcel.obtain();
android.os.Parcel _reply = android.os.Parcel.obtain();
java.lang.String _result;
try {
_data.writeInterfaceToken(DESCRIPTOR);
_data.writeString(appCode);
_data.writeInt(((lastAppToUpdate)?(1):(0)));
mRemote.transact(Stub.TRANSACTION_confirmForceUpdate, _data, _reply, 0);
_reply.readException();
_result = _reply.readString();
}
finally {
_reply.recycle();
_data.recycle();
}
return _result;
}
@Override public void updateApplicationSync(java.lang.String appCode) throws android.os.RemoteException
{
android.os.Parcel _data = android.os.Parcel.obtain();
android.os.Parcel _reply = android.os.Parcel.obtain();
try {
_data.writeInterfaceToken(DESCRIPTOR);
_data.writeString(appCode);
mRemote.transact(Stub.TRANSACTION_updateApplicationSync, _data, _reply, 0);
_reply.readException();
}
finally {
_reply.recycle();
_data.recycle();
}
}
@Override public java.lang.String rawQueryList(java.lang.String sql, java.lang.String selectionArgs) throws android.os.RemoteException
{
android.os.Parcel _data = android.os.Parcel.obtain();
android.os.Parcel _reply = android.os.Parcel.obtain();
java.lang.String _result;
try {
_data.writeInterfaceToken(DESCRIPTOR);
_data.writeString(sql);
_data.writeString(selectionArgs);
mRemote.transact(Stub.TRANSACTION_rawQueryList, _data, _reply, 0);
_reply.readException();
_result = _reply.readString();
}
finally {
_reply.recycle();
_data.recycle();
}
return _result;
}
@Override public java.lang.String rawQueryCiudades(java.lang.String sql, java.lang.String selectionArgs) throws android.os.RemoteException
{
android.os.Parcel _data = android.os.Parcel.obtain();
android.os.Parcel _reply = android.os.Parcel.obtain();
java.lang.String _result;
try {
_data.writeInterfaceToken(DESCRIPTOR);
_data.writeString(sql);
_data.writeString(selectionArgs);
mRemote.transact(Stub.TRANSACTION_rawQueryCiudades, _data, _reply, 0);
_reply.readException();
_result = _reply.readString();
}
finally {
_reply.recycle();
_data.recycle();
}
return _result;
}
@Override public java.lang.String query(java.lang.String table, java.lang.String columns, java.lang.String selection, java.lang.String selectionArgs, java.lang.String groupBy, java.lang.String having, java.lang.String orderBy) throws android.os.RemoteException
{
android.os.Parcel _data = android.os.Parcel.obtain();
android.os.Parcel _reply = android.os.Parcel.obtain();
java.lang.String _result;
try {
_data.writeInterfaceToken(DESCRIPTOR);
_data.writeString(table);
_data.writeString(columns);
_data.writeString(selection);
_data.writeString(selectionArgs);
_data.writeString(groupBy);
_data.writeString(having);
_data.writeString(orderBy);
mRemote.transact(Stub.TRANSACTION_query, _data, _reply, 0);
_reply.readException();
_result = _reply.readString();
}
finally {
_reply.recycle();
_data.recycle();
}
return _result;
}
}
static final int TRANSACTION_getCurrentUser = (android.os.IBinder.FIRST_CALL_TRANSACTION + 0);
static final int TRANSACTION_getServerURL = (android.os.IBinder.FIRST_CALL_TRANSACTION + 1);
static final int TRANSACTION_isTransTypePartial = (android.os.IBinder.FIRST_CALL_TRANSACTION + 2);
static final int TRANSACTION_getAplicacionPerfilById = (android.os.IBinder.FIRST_CALL_TRANSACTION + 3);
static final int TRANSACTION_sync = (android.os.IBinder.FIRST_CALL_TRANSACTION + 4);
static final int TRANSACTION_fetchUser = (android.os.IBinder.FIRST_CALL_TRANSACTION + 5);
static final int TRANSACTION_login = (android.os.IBinder.FIRST_CALL_TRANSACTION + 6);
static final int TRANSACTION_getDispositivoMovil = (android.os.IBinder.FIRST_CALL_TRANSACTION + 7);
static final int TRANSACTION_getNotificacionById = (android.os.IBinder.FIRST_CALL_TRANSACTION + 8);
static final int TRANSACTION_changeSession = (android.os.IBinder.FIRST_CALL_TRANSACTION + 9);
static final int TRANSACTION_hasAccess = (android.os.IBinder.FIRST_CALL_TRANSACTION + 10);
static final int TRANSACTION_getIdAplicacionPerfilDefaultBy = (android.os.IBinder.FIRST_CALL_TRANSACTION + 11);
static final int TRANSACTION_getCampoBy = (android.os.IBinder.FIRST_CALL_TRANSACTION + 12);
static final int TRANSACTION_getAplPerfilSeccionCampoById = (android.os.IBinder.FIRST_CALL_TRANSACTION + 13);
static final int TRANSACTION_sendError = (android.os.IBinder.FIRST_CALL_TRANSACTION + 14);
static final int TRANSACTION_generateReport = (android.os.IBinder.FIRST_CALL_TRANSACTION + 15);
static final int TRANSACTION_getListBinarioPathBy = (android.os.IBinder.FIRST_CALL_TRANSACTION + 16);
static final int TRANSACTION_exportDataToFile = (android.os.IBinder.FIRST_CALL_TRANSACTION + 17);
static final int TRANSACTION_getLastAplicacionBinarioVersionByCodigoAplicacion = (android.os.IBinder.FIRST_CALL_TRANSACTION + 18);
static final int TRANSACTION_confirmForceUpdate = (android.os.IBinder.FIRST_CALL_TRANSACTION + 19);
static final int TRANSACTION_updateApplicationSync = (android.os.IBinder.FIRST_CALL_TRANSACTION + 20);
static final int TRANSACTION_rawQueryList = (android.os.IBinder.FIRST_CALL_TRANSACTION + 21);
static final int TRANSACTION_rawQueryCiudades = (android.os.IBinder.FIRST_CALL_TRANSACTION + 22);
static final int TRANSACTION_query = (android.os.IBinder.FIRST_CALL_TRANSACTION + 23);
}
public java.lang.String getCurrentUser() throws android.os.RemoteException;
public java.lang.String getServerURL() throws android.os.RemoteException;
public boolean isTransTypePartial() throws android.os.RemoteException;
public java.lang.String getAplicacionPerfilById(int aplicacionPerfilId) throws android.os.RemoteException;
public java.lang.String sync(java.lang.String clazz, java.lang.String table, int version) throws android.os.RemoteException;
public java.lang.String fetchUser(int userID) throws android.os.RemoteException;
public java.lang.String login(java.lang.String username, java.lang.String password) throws android.os.RemoteException;
public java.lang.String getDispositivoMovil() throws android.os.RemoteException;
public java.lang.String getNotificacionById(int notificacionID) throws android.os.RemoteException;
public void changeSession(java.lang.String username, java.lang.String password) throws android.os.RemoteException;
public boolean hasAccess(int usuarioID, java.lang.String codAplicacion) throws android.os.RemoteException;
public java.lang.String getIdAplicacionPerfilDefaultBy(java.lang.String codAplicacion) throws android.os.RemoteException;
public java.lang.String getCampoBy(int campoId, int aplicacionPerfilId) throws android.os.RemoteException;
public java.lang.String getAplPerfilSeccionCampoById(int aplPerfilSeccionCampoId) throws android.os.RemoteException;
public void sendError(int tipoReg) throws android.os.RemoteException;
public java.lang.String generateReport(java.lang.String jsonData, java.lang.String jsonSection, java.lang.String jsonTemplate) throws android.os.RemoteException;
public java.lang.String getListBinarioPathBy(java.lang.String codAplicacion, java.lang.String tipoBinario) throws android.os.RemoteException;
public java.lang.String exportDataToFile(java.lang.String jSonData) throws android.os.RemoteException;
public java.lang.String getLastAplicacionBinarioVersionByCodigoAplicacion(java.lang.String appCode) throws android.os.RemoteException;
public java.lang.String confirmForceUpdate(java.lang.String appCode, boolean lastAppToUpdate) throws android.os.RemoteException;
public void updateApplicationSync(java.lang.String appCode) throws android.os.RemoteException;
public java.lang.String rawQueryList(java.lang.String sql, java.lang.String selectionArgs) throws android.os.RemoteException;
public java.lang.String rawQueryCiudades(java.lang.String sql, java.lang.String selectionArgs) throws android.os.RemoteException;
public java.lang.String query(java.lang.String table, java.lang.String columns, java.lang.String selection, java.lang.String selectionArgs, java.lang.String groupBy, java.lang.String having, java.lang.String orderBy) throws android.os.RemoteException;
}

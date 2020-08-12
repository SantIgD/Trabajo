package coop.tecso.hcd.helpers;

import java.util.Date;

/**
 * Notificacion para Informe Medico (notificaciones de tipo Generales, Alertas Medicas o Errores)
 * 
 * @author tecso
 *
 */
public final class IMNotification {

	private int id;
	private String code;
	private IMNotificationType imNotificationType;
	private Integer atencionId;
	private Date date;
	private String briefDescription;
	private String fullDescription;
	private int icon;	// TODO ver bien tipo de dato
	private int sound;   // TODO ver bien tipo de dato
	private boolean readed;
	private boolean alwaysVisible;
	private boolean readForced;
	
	// Constructors
	public IMNotification() {
		super();
	}
	
	public IMNotification(int id, String code, IMNotificationType imNotificationType,
			Integer atencionId, Date date, String briefDescription,
			String fullDescription, Integer icono, Integer sound, boolean readed,
			boolean alwaysVisible, boolean readForced) {
		super();
		this.id = id;
		this.code = code;
		this.imNotificationType = imNotificationType;
		this.atencionId = atencionId;
		this.date = date;
		this.briefDescription = briefDescription;
		this.fullDescription = fullDescription;
		this.icon = icono;
		this.sound = sound;
		this.readed = readed;
		this.alwaysVisible = alwaysVisible;
		this.readForced = readForced;
	}

	// Getters And Setters
	public int getId() {
		return id;
	}
	public void setId(int id) {
		this.id = id;
	}
	public String getCode() {
		return code;
	}
	public void setCode(String code) {
		this.code = code;
	}
	public IMNotificationType getIMNotificationType() {
		return imNotificationType;
	}
	public void setIMNotificationType(IMNotificationType imNotificationType) {
		this.imNotificationType = imNotificationType;
	}
	public Integer getAtencionId() {
		return atencionId;
	}
	public void setAtencionId(Integer atencionId) {
		this.atencionId = atencionId;
	}
	public Date getDate() {
		return date;
	}
	public void setDate(Date date) {
		this.date = date;
	}
	public String getBriefDescription() {
		return briefDescription;
	}
	public void setBriefDescription(String briefDescription) {
		this.briefDescription = briefDescription;
	}
	public String getFullDescription() {
		return fullDescription;
	}
	public void setFullDescription(String fullDescription) {
		this.fullDescription = fullDescription;
	}
	public int getIcon() {
		return icon;
	}
	public void setIcon(Integer icon) {
		this.icon = icon;
	}
	public int getSound() {
		return sound;
	}
	public void setSound(Integer sound) {
		this.sound = sound;
	}
	public boolean isReaded() {
		return readed;
	}
	public void setReaded(boolean readed) {
		this.readed = readed;
	}
	public boolean isAlwaysVisible() {
		return alwaysVisible;
	}
	public void setAlwaysVisible(boolean alwaysVisible) {
		this.alwaysVisible = alwaysVisible;
	}
	public boolean isReadForced() {
		return readForced;
	}
	public void setReadForced(boolean readForced) {
		this.readForced = readForced;
	}
}
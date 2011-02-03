package cz.incad.kramerius.rights.server.arragements.triggers;

import java.io.UnsupportedEncodingException;
import java.security.NoSuchAlgorithmException;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.mail.MessagingException;
import javax.mail.internet.AddressException;

import org.apache.catalina.users.AbstractUser;
import org.aplikator.client.data.RecordDTO;
import org.aplikator.client.descriptor.PropertyDTO;
import org.aplikator.server.Context;
import org.aplikator.server.persistence.PersisterTriggers;

import cz.incad.kramerius.rights.server.Mailer;
import cz.incad.kramerius.rights.server.Structure;
import cz.incad.kramerius.rights.server.utils.GeneratePasswordUtils;
import cz.incad.kramerius.rights.server.utils.GetAdminGroupIds;
import cz.incad.kramerius.security.utils.PasswordDigest;

public class UserTriggers extends AbstractUserTriggers implements PersisterTriggers {
	
	public static final Logger LOGGER = Logger.getLogger(UserTriggers.class.getName());
	
	private Structure structure;
	private Mailer mailer;
	
	public UserTriggers(Structure structure) {
		super();
		this.structure = structure;
	}

	@Override
	public RecordDTO beforeCreate(RecordDTO record, Context ctx) {
		try {
			List<Integer> groupsList = GetAdminGroupIds.getAdminGroupId(ctx);
			PropertyDTO personalAdminDTO = structure.user.PERSONAL_ADMIN.clientClone(ctx);
			record.setValue(personalAdminDTO, groupsList.get(0));

			PropertyDTO pswdDTO = structure.user.PASSWORD.clientClone(ctx);
			String generated = GeneratePasswordUtils.generatePswd();

			GeneratePasswordUtils.sendGeneratedPasswordToMail((String) record.getValue(structure.user.EMAIL.clientClone(ctx)), generated, mailer);
			
			record.setValue(pswdDTO, PasswordDigest.messageDigest(generated));
			
		} catch (NoSuchAlgorithmException e) {
			LOGGER.log(Level.SEVERE, e.getMessage(),e);
		} catch (UnsupportedEncodingException e) {
			LOGGER.log(Level.SEVERE, e.getMessage(),e);
		} catch (AddressException e) {
			LOGGER.log(Level.SEVERE, e.getMessage(),e);
		} catch (MessagingException e) {
			LOGGER.log(Level.SEVERE, e.getMessage(),e);
		}
		return record;
	}

	@Override
	public RecordDTO afterCreate(RecordDTO record, Context ctx) {
		return null;
	}

	@Override
	public RecordDTO beforeUpdate(RecordDTO recordDTO, Context ctx) {
		PropertyDTO pswdDTO = structure.user.PASSWORD.clientClone(ctx);
		recordDTO.setNotForSave(pswdDTO, true);

		PropertyDTO personalAdminDTO = structure.user.PERSONAL_ADMIN.clientClone(ctx);
		recordDTO.setNotForSave(personalAdminDTO, true);

		return null;
	}

	@Override
	public RecordDTO afterUpdate(RecordDTO recordDTO, Context ctx) {
		return null;
	}

	@Override
	public RecordDTO beforeDelete(RecordDTO recordDTO, Context ctx) {
		return null;
	}

	@Override
	public RecordDTO afterDelete(RecordDTO recordDTO, Context ctx) {
		return null;
	}

	public Mailer getMailer() {
		return mailer;
	}

	public void setMailer(Mailer mailer) {
		this.mailer = mailer;
	}

	
	
}

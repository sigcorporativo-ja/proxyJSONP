/**
 * Copyright 2010 Guadaltel, S.A.
 *
 * Licensed under the EUPL, Version 1.1 or - as soon they
 * will be approved by the European Commission - subsequent
 * versions of the EUPL (the "Licence");
 * you may not use this work except in compliance with the
 * Licence.
 * You may obtain a copy of the Licence at:
 *
 * http://ec.europa.eu/idabc/eupl
 *
 * Unless required by applicable law or agreed to in
 * writing, software distributed under the Licence is
 * distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either
 * express or implied.
 * See the Licence for the specific language governing
 * permissions and limitations under the Licence.
 */

package es.juntadeandalucia.framework.ticket.impl;

import static org.apache.commons.collections.MapUtils.toProperties;
import static org.apache.commons.collections.MapUtils.typedMap;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.ResourceBundle;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.DESedeKeySpec;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.configuration.Configuration;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import es.juntadeandalucia.framework.ticket.Ticket;
import es.juntadeandalucia.framework.ticket.utils.Base32;

@SuppressWarnings("unchecked")
public class DefaultTicket implements Ticket {

	public static final String TIME_TICKET_LIFETIME = "lifetime";//$NON-NLS-1$
	public static final String TICKET_KEY = "key";//$NON-NLS-1$
	public static final String TIME_TICKET_EXPIRY = "ticket.expirytime"; //$NON-NLS-1$

	private static final ResourceBundle msg = ResourceBundle.getBundle("es.juntadeandalucia.framework.ticket.impl.messages"); //$NON-NLS-1$ 

	private static Log log = LogFactory.getLog(DefaultTicket.class);

	private SecretKey key;
	private long ticketLifeTime;

	public DefaultTicket(Configuration config) throws Exception {
		try {
			ticketLifeTime = config.getLong(TIME_TICKET_LIFETIME, 0);
		} catch (Exception e) {
			e = new Exception(msg.getString("ticket.error.lifetimeerror")); //$NON-NLS-1$
			log.warn(e);
			throw e;
		}

		List<?> textKey = config.getList(TICKET_KEY);

		try {
			SecretKeyFactory kf = SecretKeyFactory.getInstance("DESede");
			key = kf.generateSecret(new DESedeKeySpec(hexToByte(textKey)));
		} catch (Exception e) {
			e = new Exception(msg.getString("ticket.error.keycreationerror")); //$NON-NLS-1$
			log.warn(e);
			throw e;
		}
	}

	public String getTicket(Map<String, String> map) throws Exception {

		if (ticketLifeTime > 0) {
			map.put(TIME_TICKET_EXPIRY, Long.toString(getCurrentTimeMillisUTC() + ticketLifeTime));
		}

		try {
			ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
			Properties properties = toProperties(map);
			properties.store(outputStream, "ticket component"); //$NON-NLS-1$
			byte[] bs = crypt(outputStream.toByteArray());
			return Base32.encode(bs);
		} catch (Exception e) {
			e = new Exception(msg.getString("ticket.error.ticketobtainingerror")); //$NON-NLS-1$
			log.warn(e); //$NON-NLS-1$
			throw e;
		}
		// return null;
	}

	public Map<String, String> getProperties(String ticket) throws Exception {

		byte[] propertiesBytes = decrypt(Base32.decode(ticket));
		Properties properties = new Properties();
		properties.load(new ByteArrayInputStream(propertiesBytes));

		if (ticketLifeTime > 0) {

			long currentTimeMillis = getCurrentTimeMillisUTC();
			long ticketExpiryTime;
			String expiryTime = properties.getProperty(TIME_TICKET_EXPIRY);

			if (expiryTime == null) {
				Exception e = new Exception(msg.getString("ticket.error.noexpirytime")); //$NON-NLS-1$
				log.error(e);
				throw e;
			}

			try {
				ticketExpiryTime = Long.parseLong(expiryTime);

			} catch (NumberFormatException nfe) {
				Exception e = new Exception(msg.getString("ticket.error.invalidexpirytime"), nfe); //$NON-NLS-1$
				log.error(e);
				throw e;
			}

			if (ticketExpiryTime - currentTimeMillis < 0) {

				Exception e = new Exception(msg.getString("ticket.error.ticketexpired")); //$NON-NLS-1$
				log.warn(e);
				throw e;

			}
			properties.remove(TIME_TICKET_EXPIRY);
		}

		return typedMap(properties, String.class, String.class);

	}

	private long getCurrentTimeMillisUTC() {

		return System.currentTimeMillis();

	}

	private byte[] crypt(byte[] input) throws Exception {

		Cipher cipher = Cipher.getInstance("DESede");
		cipher.init(Cipher.ENCRYPT_MODE, key);
		return cipher.doFinal(input);

	}

	private byte[] decrypt(byte[] input) throws Exception {

		Cipher cipher = Cipher.getInstance("DESede");
		cipher.init(Cipher.DECRYPT_MODE, key);
		return cipher.doFinal(input);

	}

	private byte[] hexToByte(List<?> key) throws Exception {

		ByteArrayOutputStream out = new ByteArrayOutputStream();
		for (Iterator<?> it = key.iterator(); it.hasNext();) {
			String n = (String) it.next();

			// Eliminamos los dos primeros caracteres si son "0x" o el �ltimo
			// caracter si es "h"
			if (n.startsWith("0x") && key.size() > 1) {
				n = n.substring(2);
			} else if (n.endsWith("h") && key.size() > 1) {
				n = n.substring(0, n.length() - 1);
			} else {
				try {
					byte bs[] = n.getBytes();
					return Base64.decodeBase64(bs);
				} catch (Exception e) {
					e = new Exception(msg.getString("ticket.error.invalidformatkey"));//$NON-NLS-1$
					log.warn(e);
					throw e;
				}
			}

			// Si no es múltiplo de dos, agregamos un cero por delante de la
			// clave.
			if (n.length() % 2 != 0) {
				n = "0" + n.substring(0, n.length());
			}

			// Procesamos los bloques de 2 bytes
			// for (int i = 0; i <= n.length()/2; i++) {
			try {
				out.write(Integer.parseInt(n, 16));
				// .substring(2*i, 2*i + 1)
			} catch (Exception e) {
				e = new Exception(msg.getString("ticket.error.invalidformatkey")); //$NON-NLS-1$
				log.warn(e);
				throw e;
			}
		}
		// }
		return out.toByteArray();
	}

}

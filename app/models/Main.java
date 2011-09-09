package models;

import java.io.File;
import java.util.concurrent.TimeUnit;

import javax.xml.namespace.QName;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.TransformerException;

import org.petalslink.dsb.commons.service.api.Service;
import org.petalslink.dsb.notification.client.http.HTTPNotificationConsumerClient;
import org.petalslink.dsb.notification.client.http.HTTPNotificationProducerClient;
import org.petalslink.dsb.notification.client.http.HTTPNotificationProducerRPClient;
//import org.petalslink.dsb.notification.service.NotificationConsumerService;
import org.petalslink.dsb.soap.CXFExposer;
import org.petalslink.dsb.soap.api.Exposer;
import org.w3c.dom.Document;

import com.ebmwebsourcing.easycommons.xml.XMLHelper;
import com.ebmwebsourcing.wsstar.basefaults.datatypes.impl.impl.WsrfbfModelFactoryImpl;
import com.ebmwebsourcing.wsstar.basenotification.datatypes.api.abstraction.Notify;
import com.ebmwebsourcing.wsstar.basenotification.datatypes.api.abstraction.Subscribe;
import com.ebmwebsourcing.wsstar.basenotification.datatypes.api.abstraction.SubscribeResponse;
import com.ebmwebsourcing.wsstar.basenotification.datatypes.api.utils.WsnbException;
import com.ebmwebsourcing.wsstar.basenotification.datatypes.impl.impl.WsnbModelFactoryImpl;
import com.ebmwebsourcing.wsstar.resource.datatypes.impl.impl.WsrfrModelFactoryImpl;
import com.ebmwebsourcing.wsstar.resourcelifetime.datatypes.impl.impl.WsrfrlModelFactoryImpl;
import com.ebmwebsourcing.wsstar.resourceproperties.datatypes.impl.impl.WsrfrpModelFactoryImpl;
import com.ebmwebsourcing.wsstar.topics.datatypes.api.WstopConstants;
import com.ebmwebsourcing.wsstar.topics.datatypes.impl.impl.WstopModelFactoryImpl;
import com.ebmwebsourcing.wsstar.wsnb.services.INotificationConsumer;
import com.ebmwebsourcing.wsstar.wsnb.services.INotificationProducer;
import com.ebmwebsourcing.wsstar.wsnb.services.INotificationProducerRP;
import com.ebmwebsourcing.wsstar.wsnb.services.impl.util.Wsnb4ServUtils;
import com.ebmwebsourcing.wsstar.wsrfbf.services.faults.AbsWSStarFault;

/**
 * The notification engine is hosted on some DSB node. Let's subscribe to events
 * and send events. We will check if all is working...
 * 
 * @author chamerling
 * 
 */
public class Main {

	static {
		Wsnb4ServUtils.initModelFactories(new WsrfbfModelFactoryImpl(), new WsrfrModelFactoryImpl(),
				new WsrfrlModelFactoryImpl(), new WsrfrpModelFactoryImpl(), new WstopModelFactoryImpl(),
				new WsnbModelFactoryImpl());
	}

	/**
	 * @param args
	 */
	public static void main(String[] args) {

		System.out.println("****** CREATING LOCAL SERVER ******");

		// local address which will receive notifications
		String address = "http://www.postbin.org/1ken30n";
		// DSB adress to subscribe to
		String dsbSubscribe = "http://www.postbin.org/1ken30n";
		// DSB address to send notifications to
		String dsbNotify = "http://www.postbin.org/1ken30n";

		// the one which will receive notifications
		System.out.println("Creating service which will receive notification messages from the DSB...");

		Service server = null;
		QName interfaceName = new QName("http://docs.oasis-open.org/wsn/bw-2", "NotificationConsumer");
		QName serviceName = new QName("http://docs.oasis-open.org/wsn/bw-2", "NotificationConsumerService");
		QName endpointName = new QName("http://docs.oasis-open.org/wsn/bw-2", "NotificationConsumerPort");
		// expose the service
		INotificationConsumer consumer = new INotificationConsumer() {
			public void notify(Notify notify) throws WsnbException {
				System.out
						.println("Got a notify on HTTP service, this notification comes from the DSB itself...");

				Document dom = Wsnb4ServUtils.getWsnbWriter().writeNotifyAsDOM(notify);
				System.out.println("==============================");
				try {
					XMLHelper.writeDocument(dom, System.out);
				} catch (TransformerException e) {
				}
				System.out.println("==============================");
			}
		};

		System.out.println("****** SUBSCRIBE TO NOTIFICATION ******");

		// Create the subscription, ie as a client let's send a subscribe //
		// message with reference to the previously registered endpoint
		System.out.println("Subscribing to receive DSB notifications...");
		INotificationProducer producerClient = new HTTPNotificationProducerClient(dsbSubscribe);
		Subscribe subscribe = loadSubscribe();
		try {
			SubscribeResponse response = producerClient.subscribe(subscribe);
			System.out.println("Got a response from the DSB");
			Document dom = Wsnb4ServUtils.getWsnbWriter().writeSubscribeResponseAsDOM(response);
			XMLHelper.writeDocument(dom, System.out);
		} catch (WsnbException e) {
			e.printStackTrace();
		} catch (AbsWSStarFault e) {
			e.printStackTrace();
		} catch (TransformerException e) {
			e.printStackTrace();
		}

		
		System.out.println("****** SEND NOTIFICATION TO THE DSB ******");

		// send a notification to the DSB, since we just subscribed, we should
		// receive it back...
		System.out.println("Sending a notification to the DSB...");
		INotificationConsumer consumerClient = new HTTPNotificationConsumerClient(dsbNotify);
		Notify notify = loadNotify();
		for (int i = 0; i < 10; i++) {
			try {
				consumerClient.notify(notify);
			} catch (WsnbException e) {
				e.printStackTrace();
			}
		}

		try {
			System.out.println("Waiting...");

			TimeUnit.SECONDS.sleep(10);
		} catch (InterruptedException e) {
		}

		/*
		System.out.println("****** GET RESOURCE PROPERTIES ******");

		// getting resources
		INotificationProducerRP resourceClient = new HTTPNotificationProducerRPClient(dsbSubscribe);
		try {
			QName qname = WstopConstants.TOPIC_SET_QNAME;
			com.ebmwebsourcing.wsstar.resourceproperties.datatypes.api.abstraction.GetResourcePropertyResponse response = resourceClient
					.getResourceProperty(qname);
			System.out.println("Get Resource response :");
			Document dom = Wsnb4ServUtils.getWsrfrpWriter().writeGetResourcePropertyResponseAsDOM(response);
			XMLHelper.writeDocument(dom, System.out);
		} catch (Exception e) {
			e.printStackTrace();
		}

		System.out.println("-Bye");
		*/

	}

	/**
	 * @return
	 */
	private static Notify loadNotify() {
		DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
		factory.setNamespaceAware(true);
		Document document;
		try {
			document = factory.newDocumentBuilder().parse(
					new File("E:/workspace/webapp/public/xml/notify.xml"));

			return Wsnb4ServUtils.getWsnbReader().readNotify(document);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}

	/**
	 * @return
	 */
	private static Subscribe loadSubscribe() {
		DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
		factory.setNamespaceAware(true);
		Document document;
		try {
			document = factory.newDocumentBuilder().parse(
					new File("E:/workspace/webapp/public/xml/subscribe.xml"));

			return Wsnb4ServUtils.getWsnbReader().readSubscribe(document);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}
}

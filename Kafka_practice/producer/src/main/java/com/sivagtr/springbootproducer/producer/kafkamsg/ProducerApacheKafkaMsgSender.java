package com.sivagtr.springbootproducer.producer.kafkamsg;

import java.util.Properties;

import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import com.sivagtr.springbootproducer.producer.config.PropertiesConfig;
import com.sivagtr.springbootproducer.producer.model.InformationModel;

import lombok.extern.slf4j.Slf4j;

/**
 * Producer using Apache configuration for sending messages
 */
@Component
@Slf4j
public class ProducerApacheKafkaMsgSender {

	KafkaProducer<String,String> msgKafkaProducer;

	@Autowired
	PropertiesConfig config;

	public void sendMsg(String msg) {
		log.info("Msg before Kafka sending " + msg);
		String topic = config.getMsgTopic();
		log.info("Topic : " + topic);

		ProducerRecord<String,String> msgProducer = new ProducerRecord<>(topic,topic,msg);
		msgKafkaProducer.send(msgProducer);
	}

	public void initializeKafkaPrducer(){
		if(msgKafkaProducer == null) {
			Properties properties = getProperties();
			msgKafkaProducer = new KafkaProducer<String, String>(properties);
		}
	}

	private Properties getProperties(){
		Properties properties = new Properties();
		properties.put("bootstrap.servers", config.getKafkaServer());
		properties.put("key.serializer", config.getKey());
		properties.put("value.serializer", config.getMsgValue());
		// Настройка для SSL
		properties.put("security.protocol", "SASL_SSL");
		properties.put("ssl.truststore.location", "C:\\MAI\\keystore_reg.pfx");
		properties.put("ssl.truststore.password", "123456");
		properties.put("ssl.keystore.location", "C:\\MAI\\keystore_reg.pfx");
		properties.put("ssl.keystore.password", "123456");
		properties.put("ssl.key.password", "123456");

		// Настройка для SASL
		properties.put("sasl.mechanism", "PLAIN");
		properties.put("sasl.jaas.config", "org.apache.kafka.common.security.plain.PlainLoginModule required username=\"adminkafka\" password=\"39QCgYrPLU\";");
		return properties;
	}

	public void sendJson(InformationModel informationModel){
		initializeKafkaPrducer();
		log.info("Msg before Kafka sending " + informationModel.toString());
		String topic = config.getMsgTopic();
		log.info("Topic : " + topic);

		ProducerRecord<String,String> msgProducer = new ProducerRecord<>(topic,topic,informationModel.toString());
		msgKafkaProducer.send(msgProducer);
	}
}

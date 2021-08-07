package com.example.KafkaApi.Controller;

import com.example.KafkaApi.Domain.TopicSpec;
import com.example.KafkaApi.Service.*;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Set;
import java.util.concurrent.ExecutionException;

import static java.util.logging.Logger.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@RestController
@RequestMapping("/")
public final class KafkaController {
    @Autowired
    private MessagingService messagingService;
    private static final Logger logger = LoggerFactory.getLogger(KafkaController.class);

    @PostMapping(value = "/publish")
    public ResponseEntity<String> sendMessageToKafkaTopic() {
        ResponseEntity<String> response = null;
        try {
            messagingService.publishMessage();
            response = new ResponseEntity<String>("Message sent", HttpStatus.OK);
        }
        catch(Exception e) {
            logger.error("System Error:",e.getMessage());
            response = new ResponseEntity<String>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
        return response;
    }

    @GetMapping(value = "/list")
    public ResponseEntity<Set<String>> list(){
        Set<String> result;
        result = messagingService.getTopics();
        return new ResponseEntity<Set<String>>(result, HttpStatus.OK);
    }

    @PostMapping(value = "/topic")
    public ResponseEntity<String> createTopic(@RequestBody TopicSpec topicSpec){
        try {
            messagingService.createTopic(topicSpec);
            return ResponseEntity.ok().body("created the topic " + topicSpec.getTopicName() + "\nNo of Partitions= " + topicSpec.getPartitionCount()
                    + "\nNo of Replicas = " + topicSpec.getReplicasCount());
        }
        catch (Exception e){
            logger.error("System Error:",e.getMessage());
            return new ResponseEntity<String>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @GetMapping(value = "/describe/{topic}")
    public ResponseEntity<String> describeTopic(@PathVariable("topic") String topicName){
        try {
            boolean res = messagingService.describeTopic(topicName);
            if(res)
                return ResponseEntity.ok().body("described the topic");
            else
                return ResponseEntity.ok().body("Topic is not present!!");
        }
        catch (Exception e){
            logger.error("System Error:",e.getMessage());
            return new ResponseEntity<String>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @DeleteMapping(value = "/delete")
    public ResponseEntity<String> deleteTopic(@RequestBody TopicSpec topicSpec){
        try {
            messagingService.deleteTopic(topicSpec);
            return ResponseEntity.ok().body("deleted the topic " + topicSpec.getTopicName());
        }
        catch (Exception e){
            logger.error("System Error:",e.getMessage());
            return new ResponseEntity<String>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @GetMapping(value = "/group")
    public ResponseEntity<String> describeGroup(@RequestParam String brokerUrl, @RequestParam String groupID) throws ExecutionException, InterruptedException {
        try {
            messagingService.describeGroup(brokerUrl, groupID);
            return ResponseEntity.ok().body("described the group");
        }
        catch (Exception e){
            logger.error("System Error:",e.getMessage());
            return new ResponseEntity<String>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @DeleteMapping(value = "/delete/{topic}")
    public ResponseEntity<String> deleteMsgs(@PathVariable("topic") String topicName, @RequestParam Integer partitionValue, @RequestParam Integer offsetValue){
        try {
            messagingService.deleteMessage(topicName, partitionValue, offsetValue);
            return ResponseEntity.ok().body("deleted the records");
        }
        catch (Exception e){
            logger.error("System Error:",e.getMessage());
            return new ResponseEntity<String>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @GetMapping(value = "/consume")
    public ResponseEntity<String> consumeMessage(@RequestParam String topicName, @RequestParam String groupID){
        try {
            messagingService.consumeMessage(topicName, groupID);
            return ResponseEntity.ok().body("consumed the messages");
        }
        catch (Exception e){
            logger.error("System Error:",e.getMessage());
            return new ResponseEntity<String>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

}


@ControllerAdvice
class ExceptionHelper {
    private static final Logger logger = LoggerFactory.getLogger(ExceptionHelper.class);

    @ExceptionHandler(value = { Exception.class })
    public ResponseEntity<Object> handleException(Exception ex) {
        logger.error("Exception: ",ex.getMessage());
        return new ResponseEntity<Object>(ex.getMessage(),HttpStatus.INTERNAL_SERVER_ERROR);
    }
}
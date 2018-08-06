import com.google.gson.Gson;

import java.io.*;
import java.util.*;
import java.util.stream.Collectors;

public class JsonReader {

    public List<Message> readJsonStream(InputStream in) {

        // Variable declarations
        Gson gson = new Gson();
        List<Message> messages = new ArrayList<Message>();
        com.google.gson.stream.JsonReader reader = null;

        // Counter to break and summarize after every 1000 records processed
        int counter = 0;

        try {
            reader = new com.google.gson.stream.JsonReader(new InputStreamReader(in, "UTF-8"));
            reader.beginArray();
            while (reader.hasNext()) {

                // Message - java object model for every json object
                Message message = gson.fromJson(reader, Message.class);
                messages.add(message);
                if(counter%1000==0) {

                    // Create report on messages list after every 1000 records
                    friendsMedian(messages);
                    ageMedian(messages);
                    meanBalanceAmount(messages);
                    meanUnreadActiveMessagesFemales(messages);
                    registeredUsers(messages);

                    System.out.println("\n----------------\n\n\nNext 1000 Records\n");
                    Thread.sleep(1000);

                    // clear messages list
                    messages.clear();
                }
                counter++;
            }
            reader.endArray();
            reader.close();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        return messages;
    }

    private void meanUnreadActiveMessagesFemales(List<Message> messages) {
        Double muamf = messages.stream()
                .filter(message -> message.getGender().equals("female"))
                .filter(message -> message.isActive)
                .map(x-> {
                        List<String> list =  Arrays.asList(x.getGreeting().split(" "));
                        Collections.reverse(list);
                        return Integer.parseInt(list.get(2));
                    }
                )
                .mapToInt(value -> value).average().orElse(0);

        System.out.println("  * Mean for number of Unread messages for Active females" + muamf);
    }

    private void meanBalanceAmount(List<Message> messages) {
        Double bal = messages.stream()
                .map(x->Double.parseDouble(x.getBalance().replace("$","").replaceAll(",","")))
                .mapToDouble(value -> value).average().orElse(0.0);

        System.out.println("  * Mean Balance Amount " + bal);
    }

    private void ageMedian(List<Message> messages) {
        messages.sort(Comparator.comparingDouble(Message::getAge));
        double median = messages.get(messages.size()/2).getAge();
        if(messages.size()%2 == 0)
            median = (median + messages.get(messages.size()/2-1).getAge()) / 2;

        System.out.println("  * Median age for Users " + median);
    }

    private void friendsMedian(List<Message> messages) {
        List<Integer> list = messages.stream()
                .collect(Collectors.groupingBy(x -> new ArrayList<Name>(x.getFriends())))
                .keySet()
                .stream()
                .map(e -> e.size())
                .sorted()
                .collect(Collectors.toList());

        double median = list.get(list.size()/2);
        if(list.size()%2 == 0)
            median = (median + list.get(list.size()/2-1)) / 2;

        System.out.println("  * Median for Number of Friends " + median);
    }

    private void registeredUsers(List<Message> messages) {
        System.out.println("  * Users registered in each Year");
        messages.stream().collect(Collectors.groupingBy(x->x.getRegistered().substring(0,4)))
            .entrySet()
            .stream()
            .forEach(e->
                System.out.println(e.getKey() + " " + e.getValue().size())
            );
    }

    public static void main(String[] args) throws Exception {

        if(args.length!=1) {
            System.out.println("Please provide data filename as args[0]");
            System.exit(1);
        }

        String filename = args[0];
        new JsonReader().readJsonStream(new BufferedInputStream(new FileInputStream(new File(filename))));

    }
}

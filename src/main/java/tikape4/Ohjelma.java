package tikape4;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;
import spark.Spark;
import static spark.Spark.port;

public class Ohjelma {

    public static void main(String[] args) throws Exception {
        port(getHerokuAssignedPort());
        
        // DELETE FROM Todo WHERE id = ..
        
        
        Spark.get("poista/:id", (req, res) -> {
            Connection conn = DriverManager.getConnection("jdbc:sqlite:todo.db");
            Statement stmt = conn.createStatement();
            stmt.execute("DELETE FROM Todo WHERE id = " + req.params("id"));
            conn.close();

            res.redirect("/");
            return "ok";
        });

        Spark.get("*", (req, res) -> {
            Connection conn = DriverManager.getConnection("jdbc:sqlite:todo.db");
            Statement stmt = conn.createStatement();
            ResultSet result = stmt.executeQuery("SELECT * FROM Todo");

            String vastaus = "";
            while (result.next()) {
                vastaus += result.getString("task") + " " + 
                        "<a href='poista/" + result.getString("id") + "'>X</a>" +
                        "<br/>";
            }

            String lomake = "<form method='post'>"
                    + "<input type='text' name='tehtava'/>"
                    + "<input type='submit'/>"
                    + "</form>";

            vastaus += lomake;

            conn.close();

            return vastaus;
        });

        Spark.post("*", (req, res) -> {
            Connection conn = DriverManager.getConnection("jdbc:sqlite:todo.db");
            Statement stmt = conn.createStatement();
            stmt.execute("INSERT INTO Todo (task, done) "
                    + "VALUES ('" + req.queryParams("tehtava") + "', 0)");
            
            conn.close();

            res.redirect("/");           
            return "ok";
        });

    }

    static int getHerokuAssignedPort() {
        ProcessBuilder processBuilder = new ProcessBuilder();
        if (processBuilder.environment().get("PORT") != null) {
            return Integer.parseInt(processBuilder.environment().get("PORT"));
        }
        return 4567; //return default port if heroku-port isn't set (i.e. on localhost)
    }
}

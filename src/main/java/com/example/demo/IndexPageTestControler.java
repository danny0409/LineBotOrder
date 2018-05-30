
package com.example.demo;

import org.springframework.web.bind.annotation.*;

//@RestController
public class IndexPageTestControler {
    
    //@RequestMapping("/")
    public String Home() {
        return "<html> <head> <title>TestIndexPage</title> </head>" +
                "<body> <font color=\"Green\">" +
                "<h1> It work's </h1>" +
                "</font> </body> </html>";
    }
}

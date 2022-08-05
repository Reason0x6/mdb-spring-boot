package com.FlightPub.Controllers;

import com.FlightPub.RequestObjects.*;
import com.FlightPub.Services.*;
import com.FlightPub.model.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import javax.servlet.http.HttpSession;

@Controller
public class ObjectCreationController {
    private UserAccountServices usrServices;
    private FlightServices flightServices;
    private LocationServices locationServices;
    private BookingServices bookingServices;

    private UserGroupServices groupServices;
    private UserAccount SessionUser;

    private AdminAccountServices adminAccountServices;

    @Autowired
    @Qualifier(value = "UserAccountServices")
    public void setUserService(UserAccountServices usrService) {
        this.usrServices = usrService;
    }


    @Autowired
    @Qualifier(value = "FlightServices")
    public void setFlightServices(FlightServices flightService) {
        this.flightServices = flightService;
    }

    @Autowired
    @Qualifier(value = "LocationServices")
    public void setLocationsServices(LocationServices locService) {
        this.locationServices = locService;
    }

    @Autowired
    @Qualifier(value = "BookingServices")
    public void setBookingServices(BookingServices bookingService) {
        this.bookingServices = bookingService;
    }

    @Autowired
    @Qualifier(value = "UserGroupServices")
    public void setUserGroupServices(UserGroupServices userGroupServices) {
        this.groupServices = userGroupServices;
    }

    @Autowired
    @Qualifier(value = "AdminAccountServices")
    public void setAdminServices(AdminAccountServices adminAccountServices) { this.adminAccountServices = adminAccountServices; }

    @RequestMapping("/location/add")
    public String addLoc(@ModelAttribute Location location, Model model, HttpSession session){
        model.addAttribute("usr", getSession(session));
        boolean invalid = false;

        // Ensures that all fields are filled in and valid
        if(location.getLocationID()==null || location.getLocation()==null || location.getCountry()==null || location.getDescription()==null)
            invalid = true;
        else if(location.getLocationID()=="" || location.getLocation()=="" || location.getCountry()=="" || location.getDescription()=="")
            invalid = true;
        else if(location.getLatitude() > 90 || location.getLatitude() < -90 || location.getLongitude() > 180 || location.getLongitude() < -180)
            invalid = true;


        // Checks whether the supplied input is valid then tries to performs database interaction
        if(invalid || locationServices.saveOrUpdate(location) == null)
            return "Admin/LocationManagement";

        model.addAttribute("addedLoc", location);
        return "Confirmations/NewLocation";
    }

    @RequestMapping("/usr/add") //e.g localhost:8080/usr/add?id=1&username=Toby&email=tchruches@bigpond.com&prefairport=Syd&password=123
    public String addUSR( @RequestParam String username, @RequestParam String email, @RequestParam String prefairport, @RequestParam String password, Model model, HttpSession session){
        UserAccount newUser = new UserAccount(username,email, prefairport, password, 1);
        usrServices.saveOrUpdate(newUser);

        model.addAttribute("addedUser", newUser);
        model.addAttribute("usr", getSession(session));

        return "Confirmations/NewUser";
    }

    @RequestMapping("/admin/add") //e.g localhost:8080/admin/add?id=1&firstname=alice&lastname=bob&email=bobalice@email.com&company=meta&password=123
    public String addAdminUSR( @RequestParam String email, @RequestParam String firstname, @RequestParam String lastname,@RequestParam String company, @RequestParam String password, Model model, HttpSession session){
        AdminAccount newAdmin = new AdminAccount(email, firstname, lastname, company, password, 1);
        adminAccountServices.saveOrUpdate(newAdmin);

        model.addAttribute("addedAdmin", newAdmin);
        model.addAttribute("Admin", getAdminSession(session));

        return "Confirmations/RegisterAdmin";
    }

    @PostMapping("/RegisterUser")
    public String registerUSR(@ModelAttribute UserRegister newUser, Model model, HttpSession session){
        model.addAttribute("usr", getSession(session));
        if(usrServices.getById(newUser.getEmail()) != null){
            model.addAttribute("Error", "User already exists");
            return "Error/404";
        }
        else if(newUser.isValid()){
            UserAccount nUser = new UserAccount(newUser.getFirstname(),newUser.getEmail(), newUser.getPassword());
            usrServices.saveOrUpdate(nUser);

            // TODO: Notification of new user account to be sent to newUser.getEmail()

            model.addAttribute("addedUser", nUser);
            return "Confirmations/RegisterUser";
        }

        return "/Register";
    }

    @PostMapping("/RegisterAdmin")
    public String registerAdminUSR(@ModelAttribute AdminRegister newAdmin, Model model, HttpSession session){
        model.addAttribute("Admin", getAdminSession(session));
        if(adminAccountServices.getById(newAdmin.getEmail()) != null){
            model.addAttribute("Error", "Admin already exists");
            return "Error/404";
        }
        else if(newAdmin.isValid()){
            AdminAccount admin = new AdminAccount(newAdmin.getEmail(), newAdmin.getFirstName(), newAdmin.getLastName(), newAdmin.getCompany(), newAdmin.getPassword());
            adminAccountServices.saveOrUpdate(admin);
            model.addAttribute("addedAdmin", admin);
            return "Confirmations/RegisterAdmin";
        }

        return "User/AdminRegister";
    }

    @RequestMapping("/flight/add") //e.g localhost:8080/flight/add?flightID=1021&originID=Syd&destinationID=Tam&airline=QANTAS&departure=202205101132AM&arrival=202202101231PM&flightCode=VH302&ticketprice=112.00
    public String addFlight( @ModelAttribute Flight flight, Model model, HttpSession session) {

        model.addAttribute("flight", flight);
        if(flight.getMaxSeats()<0 || flight.getBookedSeats()<0 || flight.getRating()<0 || flight.getTicketPrice()<0)
            return "Admin/FlightManagement";

        // Convert the ID to align with the Database standard)
        flight.setDestinationID(flight.getDestinationID().toUpperCase());
        flight.setOriginID(flight.getOriginID().toUpperCase());

        // Ensures that the location exists
        if(locationServices.getById(flight.getDestinationID()) == null || locationServices.getById(flight.getOriginID()) == null) {
            return "Admin/FlightManagement";
        }

        // Update the database with a update or new entry, then pass flight to the conformation page
        flightServices.saveOrUpdate(flight);
        model.addAttribute("flight", flight);
        model.addAttribute("usr", getSession(session));

        return "Confirmations/NewFlight";
    }

    @PostMapping("/group/add") //e.g localhost:8080/group/add?groupName=group1
    public String addGroup(@ModelAttribute NewGroup group, HttpSession session){
        if(!getSession(session).isLoggedIn()){
            return "redirect:/login";
        }

        UserGroup newGroup = new UserGroup(getSession(session).getEmail(), group.getGroupName());
        groupServices.saveUsers(newGroup);

        return "redirect:/Group?groupId=" + newGroup.getId();
    }

    private UserSession getSession(HttpSession session){
        UserSession sessionUser = null;
        try{
            sessionUser = (UserSession) session.getAttribute("User");
        }catch(Exception e){
            sessionUser = null;
        }

        if(sessionUser == null){
            sessionUser =  new UserSession(null);
            session.setAttribute("User", sessionUser);
        }

        return sessionUser;
    }

    private AdminSession getAdminSession(HttpSession session){
        AdminSession sessionAdmin = null;
        try{
            sessionAdmin = (AdminSession) session.getAttribute("Admin");
        } catch (Exception e){
            sessionAdmin = null;
        }

        if(sessionAdmin == null){
            sessionAdmin = new AdminSession(null);
            session.setAttribute("Admin", sessionAdmin);
        }

        return sessionAdmin;
    }


}

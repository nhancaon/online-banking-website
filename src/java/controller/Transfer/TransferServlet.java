package controller.Transfer;

import DAO.JpaDAO;
import DAO.PaymentAccountDAO;
import DAO.TransactionDAO;
import DAO.BeneficiaryDAO;
import Exception.HandleException;
import java.io.*;
import javax.servlet.*;
import javax.servlet.http.*;
import business.*;
import common.MailSender;
import controller.User.SignupServlet;
import jakarta.mail.MessagingException;
import static java.lang.Double.parseDouble;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

public class TransferServlet extends HttpServlet {

    PaymentAccountDAO paymentAccountDAO = new PaymentAccountDAO();
    TransactionDAO transactionDAO = new TransactionDAO();
    BeneficiaryDAO beneficiaryDAO = new BeneficiaryDAO();

    @Override
    protected void doPost(HttpServletRequest request,
            HttpServletResponse response)
            throws ServletException, IOException {
        String url = "/transfer.jsp";
        request.setCharacterEncoding("UTF-8");

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy - HH:mm:ss");
        // get current action
        String action = request.getParameter("action");
        if (action == null) {
            action = "return";
        } else if (action.equals("sendMail")) {
            HttpSession session = request.getSession();
            Customer customer = (Customer) session.getAttribute("customer");
            String OTP = "";
            OTP = JpaDAO.generateUniqueOTP();
            System.out.println(OTP);
            String email = customer.getEmail();
            String to = email;
            String subject = "Your OTP";
            String body = "Dear " + customer.getName() + ",\n\n"
                    + "Your OTP is " + OTP + "\n\n"
                    + "We have sent a 6-digit OTP to your email. Please check and enter the OTP for verification.\n\n"
                    + "Note: The OTP is only valid for 2 minutes.\n\n"
                    + "Sincerely,\n\n" + "NND Banking";
            session.setAttribute("OTP", OTP);
            request.setAttribute("show", "not");
            url = "/confirm.jsp";
            try {
                MailSender.sendMail(to, subject, body);
            } catch (MessagingException ex) {
                Logger.getLogger(SignupServlet.class.getName()).log(Level.SEVERE, null, ex);
            }
        } else if (action.equals("return")) {
            url = "/transfer.jsp";
        } else {
            HttpSession session = request.getSession();
            if (action.equals("add")) {
                String Number = request.getParameter("acNumber");
                String Name = request.getParameter("acName");
                String Amount = request.getParameter("acAmount");
                String Remark = request.getParameter("transRemark");
                Customer customer = (Customer) session.getAttribute("customer");
                PaymentAccount sender = null;
                sender = paymentAccountDAO.findDefaultPaymentAccount(customer.getCustomerId());
                if (sender != null) {
                    session.setAttribute("sender", sender);
                }
                PaymentAccount receiver = paymentAccountDAO.findExistingPaymentAccount(Number);
                if (receiver != null) {
                    session.setAttribute("receiver", receiver);
                }

                LocalDateTime time = LocalDateTime.now();
                String timeStr = time.format(formatter);
                session.setAttribute("timeStr", timeStr);
                session.setAttribute("Amount", Amount);
                session.setAttribute("Name", Name);
                session.setAttribute("Remark", Remark);
                session.setAttribute("receiver", receiver);
                session.setAttribute("time", time);

                try {
                    transactionDAO.checkTransaction(sender, receiver, Remark, Double.valueOf(Amount), time);
                    request.setAttribute("successMessage", "Transfer successfully");
                    url = "/confirm.jsp";
                } catch (HandleException e) {
                    url = "/transfer.jsp";
                    request.setAttribute("errorMessage", e.getMessage());

                }
            } else if (action.equals("confirm")) {
                String Amount = (String) session.getAttribute("Amount");
                String Remark = (String) session.getAttribute("Remark");
                LocalDateTime time = (LocalDateTime) session.getAttribute("time");
                String timeStr = time.format(formatter);
                PaymentAccount sender = (PaymentAccount) session.getAttribute("sender");
                PaymentAccount receiver = (PaymentAccount) session.getAttribute("receiver");
                String OTP = (String) session.getAttribute("OTP");
                String enteredOTP = request.getParameter("enteredOTP");
                Double amount = parseDouble(Amount);
                try {
                    transactionDAO.createTransaction(sender, receiver, Remark, amount, time, OTP, enteredOTP);
                    url = "/success.jsp";
                } catch (HandleException e) {
                    request.setAttribute("errorMessage", e.getMessage());
                    request.setAttribute("show", "not");
                    url = "/confirm.jsp";
                }

            }
        }

        getServletContext()
                .getRequestDispatcher(url)
                .forward(request, response);
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        ServletContext servletContext = getServletContext();

        String action = request.getParameter("action");
        String url = "/transfer.jsp";
        HttpSession session = request.getSession();
        if (action == null) {
            action = "return";
        }

        if ("reload".equals(action)) {
            url = "/transfer.jsp";
            session.removeAttribute("Amount");
            session.removeAttribute("receiver");
        }

        
        Customer customer = (Customer) session.getAttribute("customer");
        String customerId = customer.getCustomerId();
        List<Beneficiary> beneficiaries = beneficiaryDAO.findAllBeneficiaryByCustomerId(customerId);

        if (action.equals("show-name")) {
            String Number = request.getParameter("getNumber");
            String Amount = request.getParameter("getAmount");
            PaymentAccount receiver = paymentAccountDAO.findExistingPaymentAccount(Number);
            
            if (receiver == null) {
                request.setAttribute("errorMessage", "The account number is not existed");
            }
            request.setAttribute("receiver", receiver);
            request.setAttribute("Amount", Amount);
            request.setAttribute("Number", Number);
        }

        request.setAttribute("Beneficiaries", beneficiaries);
        servletContext.getRequestDispatcher(url)
                .forward(request, response);

    }

}

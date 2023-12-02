package controller.SavingAccount;

import DAO.InterestRateDAO;
import DAO.PaymentAccountDAO;
import business.SavingAccount;
import business.Customer;
import business.InterestRate;
import DAO.SavingAccountDAO;
import Exception.HandleException;
import java.io.*;
import java.time.LocalDate;
import java.util.List;
import javax.servlet.*;
import javax.servlet.http.*;
import javax.servlet.annotation.WebServlet;

@WebServlet("/create-saving-account")
public class CreateAccountServlet extends HttpServlet {

    SavingAccountDAO savingAccountDAO = new SavingAccountDAO();
    InterestRateDAO interestRateDAO = new InterestRateDAO();

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        ServletContext servletContext = getServletContext();

        String action = request.getParameter("action");

        if (action == null) {
            action = "join";
        }
        String url = "/savingAccount.jsp";
        if (action.equals("create")) {
            HttpSession session = request.getSession();
            Customer customer = (Customer) session.getAttribute("customer");
            String customerId = customer.getCustomerId();
            String accountNumber = request.getParameter("acNumber");
            String amount = request.getParameter("savingAmount");
            String savingTitle = request.getParameter("typeOfSaving");
            InterestRate interestRate = interestRateDAO.findBySavingTitle(savingTitle);

            try {
                savingAccountDAO.CreateSavingAccount(customer, accountNumber, interestRate.getSavingTitle(), interestRate.getTerm(), Double.parseDouble(amount), interestRate, interestRate.getConsecutive());
                request.setAttribute("successMessage", "Your saving account has been created successfully");
                PaymentAccountDAO paymentAccountDAO = new PaymentAccountDAO();
                List<SavingAccount> savingAccounts = savingAccountDAO.findSavingAccountByPayId(paymentAccountDAO.findDefaultPaymentAccount(customerId).getPaymentAccountId());
                request.setAttribute("savingAccounts", savingAccounts);
            } catch (HandleException e) {
                request.setAttribute("errorMessage", e.getMessage());
            }
        }
        servletContext.getRequestDispatcher(url).forward(request, response);
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        doPost(request, response);
    }
}

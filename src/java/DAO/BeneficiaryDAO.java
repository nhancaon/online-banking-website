package DAO;

import business.Beneficiary;
import Exception.HandleException;
import business.Customer;
import common.HashGenerator;
import common.MailSender;
import controller.User.SignupServlet;
import jakarta.mail.MessagingException;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

public class BeneficiaryDAO extends JpaDAO<Beneficiary> implements GenericDAO<Beneficiary> {

    @Override
    public Beneficiary create(Beneficiary t) {
        return super.create(t);
    }

    @Override
    public Beneficiary get(Object id) {
        return super.find(Beneficiary.class, id);
    }

    @Override
    public void delete(Object id) {
        super.delete(Beneficiary.class, id);
    }

    @Override
    public List<Beneficiary> listAll() {
        return super.findWithNamedQuery("");
    }

    @Override
    public long count() {

        return super.countWithNamedQuery("");
    }

    public Beneficiary findExistingBeneficiary(String accountNumber) {

        List<Beneficiary> beneficiaryList = super.findWithNamedQuery(
                "SELECT b FROM Beneficiary b WHERE b.accountNumber = :accountNumber",
                "accountNumber", accountNumber
        );

        if (!beneficiaryList.isEmpty()) {
            return beneficiaryList.get(0);
        }

        return null;
    }

    public List<Beneficiary> findAllBeneficiary() {

        List<Beneficiary> result = super.findWithNamedQuery("SELECT b FROM Beneficiary b");

        if (!result.isEmpty()) {
            return result;
        }

        return null;
    }

    public List<Beneficiary> findAllBeneficiaryByCustomerId(String customerId) {

        List<Beneficiary> beneficiaryList = super.findWithNamedQuery(
                "SELECT b FROM Beneficiary b WHERE b.customer.customerId = :customerId",
                "customerId", customerId
        );

        if (!beneficiaryList.isEmpty()) {
            return beneficiaryList;
        }

        return null;
    }

    public List<Beneficiary> findAllBeneficiaryByNameOrAccountNumber(String parameter, String customerId) throws HandleException {

        Map<String, Object> parameters = new HashMap<>();

        parameters.put("parameter", "%" + parameter + "%");
        parameters.put("customerId", customerId);

        List<Beneficiary> beneficiaryList = super.findWithNamedQuery(
                "SELECT b FROM Beneficiary b WHERE b.customer.customerId = :customerId AND (LOWER(b.Name) LIKE LOWER(:parameter) OR b.accountNumber LIKE :parameter)",
                parameters
        );

        if (!beneficiaryList.isEmpty()) {
            return beneficiaryList;
        } else {
            throw new HandleException("The Contact " + parameter + " is not existed", 409);   
        }
    }

    public Beneficiary CreateBeneficiary(String accountNumber, String nickName, Customer customer) throws HandleException {

        Beneficiary beneficiaryEntity = new Beneficiary();
        Beneficiary existingBeneficiary = findExistingBeneficiary(accountNumber);
        PaymentAccountDAO paymentAccountDAO = new PaymentAccountDAO();
        if (paymentAccountDAO.findExistingPaymentAccount(accountNumber) == null) {
            throw new HandleException("The account with number " + accountNumber
                    + " is not existed.", 409);
        } else {

            if (existingBeneficiary != null) {
                if (existingBeneficiary.getAccountNumber().equals(accountNumber)) {
                    throw new HandleException("The Beneficiary with number " + accountNumber
                            + " is already existed.", 409);
                }
            } else {

                beneficiaryEntity.setBeneficiaryId(generateUniqueId());
                beneficiaryEntity.setName(nickName);
                beneficiaryEntity.setAccountNumber(accountNumber);
                beneficiaryEntity.setCustomer(customer);
                create(beneficiaryEntity);

            }
        }

        return null;
    }

    public Beneficiary updateBeneficiary(Beneficiary bene, String beneficiaryId, String fullName) throws HandleException {
        Beneficiary beneficiaryEntityUpdate = new Beneficiary();

        beneficiaryEntityUpdate.setBeneficiaryId(beneficiaryId);
        beneficiaryEntityUpdate.setName(fullName);
        beneficiaryEntityUpdate.setAccountNumber(bene.getAccountNumber());
        beneficiaryEntityUpdate.setCustomer(bene.getCustomer());

        update(beneficiaryEntityUpdate); 
        return beneficiaryEntityUpdate;
    }

    public Beneficiary checkUpdateBeneficiary(String beneficiaryId, String fullName, String accNum) throws HandleException {
        Beneficiary duplicatedAccountNumber = findExistingBeneficiary(accNum);

        if (duplicatedAccountNumber != null){
            if(duplicatedAccountNumber.getBeneficiaryId().equals(beneficiaryId)){
                updateBeneficiary(duplicatedAccountNumber, beneficiaryId, fullName);
            }
            else{
                throw new HandleException("The beneficiary with name: " + fullName + " is already registered.", 409);
            }
        }

        return null;
    }
}

package pl.xt.jokii.gcpaymentcalculator;

/**
 * Created by Tomek on 01.06.13.
 */
public class OptionStore {
    String name;
    double payCompany;
    double payEmployee;

    OptionStore(String name, double payCompany, double payEmployee){
        this.name = name;
        this.payCompany = payCompany;
        this.payEmployee = payEmployee;
    }
}

import java.time.LocalDate;

/**
 * Created by ikownacki on 17.07.2017.
 */
public class CurrencyRepresentation {
    private String currencyCode;
    private Integer currencyMultiplier;
    private Double averageExchangeRate;
    private LocalDate publicationDate;

    public CurrencyRepresentation(String currencyCode, Integer currencyMultiplier, Double averageExchangeRate, LocalDate publicationDate) {
        this.currencyCode = currencyCode;
        this.currencyMultiplier = currencyMultiplier;
        this.averageExchangeRate = averageExchangeRate;
        this.publicationDate = publicationDate;
    }

    public Integer getCurrencyMultiplier() {
        return currencyMultiplier;
    }

    public String getCurrencyCode() {
        return currencyCode;
    }

    public double getAverageExchangeRate() {
        return averageExchangeRate;
    }

    public LocalDate getPublicationDate() {
        return publicationDate;
    }

    @Override
    public String toString() {
        return "nbp.CurrencyRepresentation{" +
                "currencyMultiplier='" + currencyMultiplier + '\'' +
                ", currencyCode='" + currencyCode + '\'' +
                ", averageExchangeRate=" + averageExchangeRate +
                ", publicationDate=" + publicationDate +
                '}';
    }
}

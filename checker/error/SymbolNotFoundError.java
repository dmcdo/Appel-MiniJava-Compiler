package checker.error;

public class SymbolNotFoundError extends SemanticsError {
    public String symbol;

    public SymbolNotFoundError(int l, int c, String symbol) {
        super(l, c);
        this.symbol = symbol;
    }
}

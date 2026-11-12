package ru.skambet.web.form;

/**
 * Feature 16: odds П1 / X / П2 typed by the administrator.
 */
public class OddsForm {

    private String oddsHome;
    private String oddsDraw;
    private String oddsAway;

    public String getOddsHome() {
        return oddsHome;
    }

    public void setOddsHome(String oddsHome) {
        this.oddsHome = oddsHome;
    }

    public String getOddsDraw() {
        return oddsDraw;
    }

    public void setOddsDraw(String oddsDraw) {
        this.oddsDraw = oddsDraw;
    }

    public String getOddsAway() {
        return oddsAway;
    }

    public void setOddsAway(String oddsAway) {
        this.oddsAway = oddsAway;
    }
}

package managers;

import javafx.stage.Modality;
import javafx.stage.StageStyle;

/**
 * Created by Gabo on 10/04/2017.
 */
public enum FXMLScenes {

    MAIN {
        @Override
        String getFXMLName() {
            return "fxmls/mainScene.fxml";
        }

        @Override
        Modality getModality() {
            return Modality.NONE;
        }

        @Override
        String getStylesheet() {
            return "stylesheets/mainStyle.css";
        }

        @Override
        public String getTitle() {
            return "Meta-Music Seeker";
        }
    }, PROGRESS {
        @Override
        String getFXMLName() {
            return "fxmls/ProgressDialog.fxml";
        }

        @Override
        Modality getModality() {
            return Modality.APPLICATION_MODAL;
        }

        @Override
        String getStylesheet() {
            return "stylesheets/dialogStyle.css";
        }

        @Override
        public String getTitle() {
            return "";
        }
    }, SPECTRUM {
        @Override
        String getFXMLName() {
            return "fxmls/spectrumScene.fxml";
        }

        @Override
        Modality getModality() {
            return Modality.NONE;
        }

        @Override
        String getStylesheet() {
            return "stylesheets/spectrumStyle.css";
        }

        @Override
        public String getTitle() {
            return "Analizador Espectral";
        }
    }, SPECTRUM_MAXIMIZED {
        @Override
        String getFXMLName() {
            return null;
        }

        @Override
        Modality getModality() {
            return null;
        }

        @Override
        String getStylesheet() {
            return null;
        }

        @Override
        public String getTitle() {
            return "";
        }
    };

    abstract String getFXMLName();
    abstract Modality getModality();
    abstract String getStylesheet();
    public abstract String getTitle();
}

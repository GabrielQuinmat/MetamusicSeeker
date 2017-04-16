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
            return "/fxmls/mainScene.fxml";
        }

        @Override
        Modality getModality() {
            return Modality.NONE;
        }

        @Override
        String getStylesheet() {
            return "/stylesheets/mainStyle.css";
        }

        @Override
        public String getTitle() {
            return "Meta-Music Seeker";
        }

        @Override
        StageStyle getStageStyle() {
            return StageStyle.DECORATED;
        }
    }, PROGRESS {
        @Override
        String getFXMLName() {
            return "/fxmls/ProgressDialog.fxml";
        }

        @Override
        Modality getModality() {
            return Modality.APPLICATION_MODAL;
        }

        @Override
        String getStylesheet() {
            return "/stylesheets/dialogStyle.css";
        }

        @Override
        public String getTitle() {
            return "";
        }

        @Override
        StageStyle getStageStyle() {
            return StageStyle.UNDECORATED;
        }
    }, SPECTRUM {
        @Override
        String getFXMLName() {
            return "/fxmls/spectrumScene.fxml";
        }

        @Override
        Modality getModality() {
            return Modality.NONE;
        }

        @Override
        String getStylesheet() {
            return "/stylesheets/spectrumStyle.css";
        }

        @Override
        public String getTitle() {
            return "Analizador Espectral";
        }

        @Override
        StageStyle getStageStyle() {
            return StageStyle.DECORATED;
        }
    }, SPECTRUM_MAXIMIZED {
        @Override
        String getFXMLName() {
            return "/fxmls/spectrumMaximizedSpectrum.fxml";
        }

        @Override
        Modality getModality() {
            return Modality.NONE;
        }

        @Override
        String getStylesheet() {
            return "/stylesheets/spectrumStyle.css";
        }

        @Override
        public String getTitle() {
            return "Espectograma Aumentado";
        }

        @Override
        StageStyle getStageStyle() {
            return StageStyle.DECORATED;
        }
    }, WAVEFORM_MAXIMIZED {
        @Override
        String getFXMLName() {
            return "/fxmls/spectrumMaximized.fxml";
        }

        @Override
        Modality getModality() {
            return Modality.NONE;
        }

        @Override
        String getStylesheet() {
            return "/stylesheets/spectrumStyle.css";
        }

        @Override
        public String getTitle() {
            return "Forma de Onda Aumentada";
        }

        @Override
        StageStyle getStageStyle() {
            return StageStyle.DECORATED;
        }
    }, ALERT_DIALOG {
        @Override
        String getFXMLName() {
            return "/fxmls/alertDialog.fxml";
        }

        @Override
        Modality getModality() {
            return Modality.APPLICATION_MODAL;
        }

        @Override
        String getStylesheet() {
            return "/stylesheets/dialogStyle.css";
        }

        @Override
        public String getTitle() {
            return null;
        }

        @Override
        StageStyle getStageStyle() {
            return StageStyle.UNDECORATED;
        }
    };

    abstract String getFXMLName();
    abstract Modality getModality();
    abstract String getStylesheet();
    public abstract String getTitle();
    abstract StageStyle getStageStyle();

}

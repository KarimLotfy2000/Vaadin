import '@univerjs/sheets/facade';
import '@univerjs/sheets-ui/facade';
import '@univerjs/presets'
import { UniverSheetsCorePreset } from '@univerjs/preset-sheets-core'
import sheetsCoreEnUS from '@univerjs/preset-sheets-core/locales/en-US'
 import { createUniver, LocaleType, mergeLocales } from '@univerjs/presets'
 import locale from "@univerjs/sheets/locale/en-US";
import '@univerjs/design/lib/index.css';
import '@univerjs/sheets-ui/lib/index.css';
import '@univerjs/preset-sheets-core/lib/index.css';



export class UniverSheet extends HTMLElement {
    private univerAPI: any;
    private initialized = false;

    private container!: HTMLDivElement;
    private initPromise: Promise<void>;
    private initResolve!: () => void;

    private pendingWorkbookJson: string | null = null;

    constructor() {
        super();
        this.initPromise = new Promise<void>((res) => (this.initResolve = res));
    }

    connectedCallback() {
        if (this.initialized) return;

        // Create container
        this.style.display = 'block';
        this.style.width = '100%';
         this.style.height = '900px';

        this.container = document.createElement('div');
        this.container.style.width = '100%';
        this.container.style.height = '100%';
        this.appendChild(this.container);

        // Init Univer
        const { univerAPI } = createUniver({
            locale: LocaleType.EN_US,
            locales: {
                [LocaleType.EN_US]: mergeLocales(sheetsCoreEnUS),
            },
            presets: [
                UniverSheetsCorePreset({
                    container: this.container,
                }),
            ],
        });

        this.univerAPI = univerAPI;
        this.initialized = true;
        this.initResolve();

        // If render was called before init
        if (this.pendingWorkbookJson) {
            const json = this.pendingWorkbookJson;
            this.pendingWorkbookJson = null;
            this.renderWorkbookJson(json);
        }
    }

    async renderWorkbookJson(workbookJson: string) {
        if (!this.initialized) {
            this.pendingWorkbookJson = workbookJson;
            return;
        }

        await this.initPromise;

        const workbookData = JSON.parse(workbookJson);
        if (!workbookData.locale) workbookData.locale = 'en-US';
        if (!workbookData.appVersion) workbookData.appVersion = '0.10.2';

        if (!this.univerAPI?.createWorkbook) {
            throw new Error('univerAPI.createWorkbook not available');
        }

        this.univerAPI.createWorkbook(workbookData);
    }
}

customElements.define('univer-sheet', UniverSheet);


import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import { DocumentImportComponent } from './document-import/document-import.component';
import {MatCardModule} from '@angular/material/card';
import {MatIconModule} from '@angular/material/icon';
import {MatMenuModule} from '@angular/material/menu';
import {FlexModule} from '@angular/flex-layout';
import {MatRippleModule} from '@angular/material/core';
import {MatProgressSpinnerModule} from '@angular/material/progress-spinner';
import {MatPaginatorModule} from '@angular/material/paginator';
import {MatTableModule} from '@angular/material/table';
import {MatButtonModule} from '@angular/material/button';
import {RouterModule} from '@angular/router';
import {MatFormFieldModule} from '@angular/material/form-field';
import {Ng8commonModule} from '../ng8common/ng8common.module';
import {MatInputModule} from '@angular/material/input';
import {MatAutocompleteModule} from '@angular/material/autocomplete';
import {FormsModule} from '@angular/forms';
import {MatTreeModule} from '@angular/material/tree';
import {MatProgressBarModule} from '@angular/material/progress-bar';
import {ConceptModule} from '../concept/concept.module';



@NgModule({
  declarations: [DocumentImportComponent],
    imports: [
        CommonModule,
        MatCardModule,
        MatIconModule,
        MatMenuModule,
        FlexModule,
        MatRippleModule,
        MatProgressSpinnerModule,
        MatPaginatorModule,
        MatTableModule,
        MatButtonModule,
        RouterModule,
        MatFormFieldModule,
        Ng8commonModule,
        MatInputModule,
        MatAutocompleteModule,
        FormsModule,
        MatTreeModule,
        MatProgressBarModule,
        ConceptModule
    ]
})
export class DocumentModule { }

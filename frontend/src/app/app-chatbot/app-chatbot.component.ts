import {Component, OnInit} from '@angular/core';
import {createChat} from '@n8n/chat';

@Component({
  selector: 'app-chatbot',
  imports: [],
  templateUrl: './app-chatbot.component.html',
})
export class AppChatbotComponent implements OnInit{

    ngOnInit(): void {
      createChat({
        webhookUrl: 'https://adrianoqueiroz.app.n8n.cloud/webhook/e985d15f-b2f6-456d-be15-97e0b1544a40/chat'
      });
    }

}

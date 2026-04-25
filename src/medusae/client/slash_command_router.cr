module Medusae
  module Client
    class SlashCommandRouter
      alias InteractionResponder = Proc(String, String, Int32, Hash(String, JSON::Any)?, Nil)
      alias ContextHandler = Proc(JSON::Any, Nil)

      enum InteractionType
        Ping
        ApplicationCommand
        MessageComponent
        ApplicationCommandAutocomplete
        ModalSubmit
        Unknown

        def self.from_code(code : Int32) : self
          case code
          when 1 then Ping
          when 2 then ApplicationCommand
          when 3 then MessageComponent
          when 4 then ApplicationCommandAutocomplete
          when 5 then ModalSubmit
          else        Unknown
          end
        end
      end

      enum CommandType
        ChatInput
        UserContext
        MessageContext
        Unknown

        def self.from_code(code : Int32) : self
          case code
          when 1 then ChatInput
          when 2 then UserContext
          when 3 then MessageContext
          else        Unknown
          end
        end
      end

      enum ResponseType
        Pong
        ChannelMessage
        DeferredChannelMessage
        DeferredMessageUpdate
        UpdateMessage
        Autocomplete
        Modal

        def code : Int32
          case self
          when Pong
            1
          when ChannelMessage
            4
          when DeferredChannelMessage
            5
          when DeferredMessageUpdate
            6
          when UpdateMessage
            7
          when Autocomplete
            8
          when Modal
            9
          else
            raise "unreachable"
          end
        end
      end

      @slash_handlers = {} of String => ContextHandler
      @user_context_handlers = {} of String => ContextHandler
      @message_context_handlers = {} of String => ContextHandler
      @component_handlers = {} of String => ContextHandler
      @modal_handlers = {} of String => ContextHandler
      @autocomplete_handlers = {} of String => ContextHandler
      @global_component_handlers = [] of ContextHandler
      @global_modal_handlers = [] of ContextHandler

      def initialize(@responder : InteractionResponder)
      end

      def register_slash_handler(command_name : String, &handler : ContextHandler) : Nil
        register_unique_handler(@slash_handlers, command_name, "slash command", handler)
      end

      def register_user_context_menu_handler(command_name : String, &handler : ContextHandler) : Nil
        register_unique_handler(@user_context_handlers, command_name, "user context menu", handler)
      end

      def register_message_context_menu_handler(command_name : String, &handler : ContextHandler) : Nil
        register_unique_handler(@message_context_handlers, command_name, "message context menu", handler)
      end

      def register_component_handler(custom_id : String, &handler : ContextHandler) : Nil
        register_unique_handler(@component_handlers, custom_id, "component", handler)
      end

      def register_global_component_handler(&handler : ContextHandler) : Nil
        @global_component_handlers << handler
      end

      def register_modal_handler(custom_id : String, &handler : ContextHandler) : Nil
        register_unique_handler(@modal_handlers, custom_id, "modal", handler)
      end

      def register_global_modal_handler(&handler : ContextHandler) : Nil
        @global_modal_handlers << handler
      end

      def register_autocomplete_handler(command_name : String, &handler : ContextHandler) : Nil
        register_unique_handler(@autocomplete_handlers, command_name, "autocomplete", handler)
      end

      def handle_interaction(interaction : JSON::Any?) : Nil
        return if interaction.nil?

        interaction_type = InteractionType.from_code(int_field(interaction, "type"))

        case interaction_type
        when InteractionType::Ping
          respond(interaction, ResponseType::Pong, nil)
        when InteractionType::ApplicationCommand
          handle_application_command(interaction)
        when InteractionType::ApplicationCommandAutocomplete
          dispatch_by_data_field(interaction, @autocomplete_handlers, "name")
        when InteractionType::MessageComponent
          dispatch_by_data_field(interaction, @component_handlers, "custom_id", @global_component_handlers)
        when InteractionType::ModalSubmit
          dispatch_by_data_field(interaction, @modal_handlers, "custom_id", @global_modal_handlers)
        when InteractionType::Unknown
          # Intentionally ignored.
        end
      end

      def respond_with_message(interaction : JSON::Any, content : String) : Nil
        respond(interaction, ResponseType::ChannelMessage, json_hash({"content" => JSON::Any.new(content)}))
      end

      def defer_message(interaction : JSON::Any) : Nil
        respond(interaction, ResponseType::DeferredChannelMessage, nil)
      end

      private def register_unique_handler(handlers : Hash(String, ContextHandler), key : String, handler_type : String, handler : ContextHandler) : Nil
        normalized_key = validate_key(key, handler_type)
        if handlers.has_key?(normalized_key)
          raise ArgumentError.new("Interaction handler already registered for #{handler_type}: #{normalized_key}")
        end
        handlers[normalized_key] = handler
      end

      private def validate_key(key : String, key_type : String) : String
        normalized = key.strip
        raise ArgumentError.new("#{key_type} key must not be blank") if normalized.empty?
        normalized
      end

      private def handle_application_command(interaction : JSON::Any) : Nil
        command_code = interaction.dig?("data", "type").try(&.as_i?) || 1
        command_type = CommandType.from_code(command_code)
        dispatch_by_data_field(interaction, handlers_for(command_type), "name")
      end

      private def dispatch_by_data_field(
        interaction : JSON::Any,
        handlers : Hash(String, ContextHandler),
        data_field : String,
        global_handlers : Array(ContextHandler) = [] of ContextHandler,
      ) : Nil
        if handler = handler_for(interaction, handlers, data_field)
          handler.call(interaction)
          return
        end

        global_handlers.each { |handler| handler.call(interaction) }
      end

      private def respond(interaction : JSON::Any, response_type : ResponseType, data : Hash(String, JSON::Any)?) : Nil
        interaction_id = non_blank_string(interaction, "id")
        interaction_token = non_blank_string(interaction, "token")

        if interaction_id.nil? || interaction_token.nil?
          raise ArgumentError.new("interaction must include id and token")
        end

        @responder.call(interaction_id, interaction_token, response_type.code, data)
      end

      private def int_field(interaction : JSON::Any, field : String) : Int32
        interaction[field]?.try(&.as_i?) || 0
      end

      private def handlers_for(command_type : CommandType) : Hash(String, ContextHandler)
        case command_type
        when .user_context?    then @user_context_handlers
        when .message_context? then @message_context_handlers
        else                        @slash_handlers
        end
      end

      private def handler_for(
        interaction : JSON::Any,
        handlers : Hash(String, ContextHandler),
        data_field : String,
      ) : ContextHandler?
        key = interaction.dig?("data", data_field).try(&.as_s?)
        return nil unless key

        handlers[key.strip]?
      end

      private def non_blank_string(interaction : JSON::Any, field : String) : String?
        value = interaction[field]?.try(&.as_s?)
        return nil unless value

        return nil if value.strip.empty?
        value
      end

      private def json_hash(named_values : Hash(String, JSON::Any)) : Hash(String, JSON::Any)
        named_values
      end
    end
  end
end
